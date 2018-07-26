<template>
  <div class="uiRightContainerArea message-list">
    <div v-if="contact && Object.keys(contact).length !== 0" id="chats" :class="{'chat-no-conversation muted': (!messages || !messages.length) && !newMessagesLoading}" class="chat-message-list" @wheel="loadMoreMessages" @scroll="loadMoreMessages">
      <div v-show="newMessagesLoading" class="center">
        <img src="/chat/img/sync.gif" width="64px" class="chatLoading">
      </div>
      <div v-for="(subMessages, dayDate) in messagesMap" :key="dayDate" class="chat-message-day">
        <div class="day-separator"><span>{{ dayDate }}</span></div>
        <exo-chat-message-detail v-for="(messageObj, i) in subMessages" :key="messageObj.clientId" :highlight="searchKeyword" :room="contact.room" :room-fullname="contact.fullName" :message="messageObj" :hide-time="isHideTime(i, subMessages)" :hide-avatar="isHideAvatar(i, subMessages)" :mini-chat="miniChat" @edit-message="editMessage"></exo-chat-message-detail>
      </div>
      <span v-show="!newMessagesLoading && (!messages || !messages.length)" class="text">{{ $t('exoplatform.chat.no.messages') }}</span>
    </div>
    <exo-chat-message-composer :contact="contact" :mini-chat="miniChat" @message-written="messageWritten"></exo-chat-message-composer>
    <exo-modal v-if="!miniChat" v-show="showEditMessageModal" :title="$t('exoplatform.chat.msg.edit')" modal-class="edit-message-modal" @modal-closed="closeModal">
      <textarea id="editMessageComposerArea" ref="editMessageComposerArea" v-model="messageToEdit.msg" name="editMessageComposerArea" autofocus @keydown.enter="preventDefault" @keypress.enter="preventDefault" @keyup.enter="saveMessage"></textarea>
      <div class="uiAction uiActionBorder">
        <div class="btn btn-primary" @click="saveMessage(false)">{{ $t('exoplatform.chat.save') }}</div>
        <div class="btn" @click="closeModal">{{ $t('exoplatform.chat.cancel') }}</div>
      </div>
    </exo-modal>
  </div>
</template>

<script>
import * as chatWebSocket from '../chatWebSocket';
import * as chatWebStorage from '../chatWebStorage';
import * as chatServices from '../chatServices';
import * as chatTime from '../chatTime';

import ExoChatMessageDetail from './ExoChatMessageDetail.vue';
import ExoChatMessageComposer from './ExoChatMessageComposer.vue';
import ExoModal from './modal/ExoModal.vue';

export default {
  components: {
    'exo-modal': ExoModal,
    'exo-chat-message-detail': ExoChatMessageDetail,
    'exo-chat-message-composer': ExoChatMessageComposer
  },
  props: {
    miniChat: {
      type: Boolean,
      default: false
    },
    minimized: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      messages: [],
      scrollToBottom: true,
      contact: {},
      messageToEdit: {},
      showEditMessageModal: false,
      totalMessagesToLoad: 0,
      searchKeyword: '',
      windowFocused: true,
      newMessagesLoading: false
    };
  },
  computed: {
    messagesMap() {
      const days = this.messages.map((message) => chatTime.getDayDate(message.timestamp).toString() ).reduce(function(result, current){
        return current && current.length && result.indexOf(current) === -1 ? result.concat(current) : result;
      }, []);
      const messagesMap = {};
      days.forEach(element => {
        const subMessages = this.messages.filter(message => chatTime.getDayDate(message.timestamp) === element);
        if(subMessages && subMessages.length) {
          messagesMap[element] = subMessages;
        }
      });
      return messagesMap;
    },
    hasMoreMessages() {
      return this.totalMessagesToLoad <= this.messages.length;
    },
    chatMessageListContainer() {
      return $('.chat-message-list');
    }
  },
  watch: {
    windowFocused(newValue) {
      if (newValue && this.contact && this.contact.room) {
        chatWebSocket.setRoomMessagesAsRead(this.contact.room);
      }
    },
    minimized(value) {
      if(this.contact && this.contact.room) {
        if(!value) {
          chatWebSocket.setRoomMessagesAsRead(this.contact.room);
          this.scrollToBottom = true;
          this.scrollToEnd();
        }
      }
    }
  },
  updated() {
    this.scrollToEnd();
  },
  created() {
    document.addEventListener(this.$constants.EVENT_MESSAGE_UPDATED, this.messageReceived);
    document.addEventListener(this.$constants.EVENT_MESSAGE_DELETED, this.messageDeleted);
    document.addEventListener(this.$constants.EVENT_MESSAGE_RECEIVED, this.messageReceived);
    document.addEventListener(this.$constants.EVENT_MESSAGE_READ, this.messageSent);
    document.addEventListener(this.$constants.EVENT_ROOM_SELECTION_CHANGED, this.contactChanged);
    document.addEventListener(this.$constants.ACTION_MESSAGE_EDIT_LAST, this.editLastMessage);
    document.addEventListener(this.$constants.ACTION_MESSAGE_SEARCH, this.searchMessage);

    $(window).focus(this.chatFocused);
    $(window).blur(this.chatFocused);
  },
  destroyed() {
    document.removeEventListener(this.$constants.EVENT_MESSAGE_UPDATED, this.messageReceived);
    document.removeEventListener(this.$constants.EVENT_MESSAGE_DELETED, this.messageDeleted);
    document.removeEventListener(this.$constants.EVENT_MESSAGE_RECEIVED, this.messageReceived);
    document.removeEventListener(this.$constants.EVENT_MESSAGE_READ, this.messageSent);
    document.removeEventListener(this.$constants.EVENT_ROOM_SELECTION_CHANGED, this.contactChanged);
    document.removeEventListener(this.$constants.ACTION_MESSAGE_EDIT_LAST, this.editLastMessage);
    document.removeEventListener(this.$constants.ACTION_MESSAGE_SEARCH, this.searchMessage);
  },
  methods: {
    messageWritten(message) {
      chatWebStorage.storeNotSentMessage(message);
      this.addOrUpdateMessageToList(message);
      this.setScrollToBottom();
      document.dispatchEvent(new CustomEvent(this.$constants.ACTION_MESSAGE_SEND, {'detail' : message}));
    },
    messageModified(message) {
      this.addOrUpdateMessageToList(message);
      this.setScrollToBottom();
      message.room = this.contact.room;
      document.dispatchEvent(new CustomEvent(this.$constants.ACTION_MESSAGE_SEND, {'detail' : message}));
    },
    messageReceived(e) {
      const messageObj = e.detail;
      const message = messageObj.data;
      this.unifyMessageFormat(messageObj, message);
      this.addOrUpdateMessageToList(message);
    },
    messageSent(e) {
      const messageObj = e.detail;
      const message = messageObj.data;
      if(message) {
        this.addOrUpdateMessageToList(message);
      }
    },
    chatFocused(e) {
      this.windowFocused = e.type === 'focus';
    },
    contactChanged(e) {
      this.messages = [];
      this.totalMessagesToLoad = 0;
      this.newMessagesLoading = false;

      this.contact = e.detail;
      if (this.contact.room) {
        this.retrieveRoomMessages(); 
      } else if (this.contact.user) {
        chatServices.getRoomId(eXo.chat.userSettings, this.contact.user, 'username').then((room) => {
          if(room) {
            this.contact.room = room;
            this.retrieveRoomMessages(); 
          }
        });
      }
    },
    setScrollToBottom: function() {
      this.scrollToBottom = true;
    },
    scrollToEnd: function(e) {
      // If triggered using an event or explicitly asked to scroll to bottom
      if (e || this.scrollToBottom) {
        this.chatMessageListContainer.scrollTop(this.chatMessageListContainer.prop('scrollHeight'));
        if (!e) {
          this.scrollToBottom = false;
        }
      }
    },
    isScrollPositionAtEnd() {
      if(this.chatMessageListContainer && this.chatMessageListContainer.length) {
        return this.chatMessageListContainer[0].scrollHeight - this.chatMessageListContainer.scrollTop() - this.chatMessageListContainer.height() < this.$constants.MAX_SCROLL_POSITION_FOR_AUTOMATIC_SCROLL;
      } else {
        return false;
      }
    },
    loadMoreMessages() {
      if (this.newMessagesLoading || this.chatMessageListContainer.scrollTop() > 0 || !this.hasMoreMessages) {
        return;
      }
      this.retrieveRoomMessages(true);
    },
    retrieveRoomMessages(avoidScrollingDown) {
      if(!this.contact || !this.contact.room || !this.contact.room.trim().length) {
        this.messages = [];
        return;
      }
      if (this.newMessagesLoading) {
        return;
      }
      let toTimestamp;
      if(!this.messages || !this.messages.length) {
        toTimestamp = '';
        this.totalMessagesToLoad = 0;
      } else {
        toTimestamp = this.messages[0].timestamp;
      }
      const limit = this.$constants.MESSAGES_PER_PAGE;
      this.newMessagesLoading = true;
      return chatServices.getRoomMessages(eXo.chat.userSettings, this.contact, toTimestamp, limit).then(data => {
        if (this.contact.room === data.room) {
          // Mark room messages as read
          document.dispatchEvent(new CustomEvent(this.$constants.ACTION_ROOM_SET_READ, {detail: this.contact.room}));

          // Scroll to bottom once messages list updated
          this.scrollToBottom = !avoidScrollingDown;

          const roomNotSentMessages = chatWebStorage.getRoomNotSentMessages(eXo.chat.userSettings.username, this.contact.room);
          data.messages.concat(roomNotSentMessages).forEach(message => {
            if (!this.messages.find(displayedMessage => displayedMessage.msgId && displayedMessage.msgId === message.msgId || displayedMessage.clientId && displayedMessage.clientId === message.clientId)) {
              this.messages.unshift(message);
            }
          });
          this.messages.sort((a, b) => {
            return a.timestamp - b.timestamp;
          });
          this.totalMessagesToLoad += limit;
        }
        this.newMessagesLoading = false;
      }).catch(() => this.newMessagesLoading = false);
    },
    findMessage(field, value) {
      return this.messages.find(message => {return message[field] === value;});
    },
    getPrevMessage(i, messages) {
      return i <= 0 && messages.length >= i ? null : messages[i-1];
    },
    isHideTime(i, messages) {
      const prevMsg = this.getPrevMessage(i, messages);
      if (prevMsg === null || this.mq === 'mobile') {
        return false;
      } else {
        return !messages[i].timestamp || chatTime.getTimeString(prevMsg.timestamp) === chatTime.getTimeString(messages[i].timestamp);
      }
    },
    isHideAvatar(i, messages) {
      const prevMsg = this.getPrevMessage(i, messages);
      if (prevMsg === null) {
        return false;
      } else {
        return prevMsg.user === messages[i].user && prevMsg.timestamp && messages[i].timestamp && chatTime.getDayDate(prevMsg.timestamp) ===  chatTime.getDayDate(messages[i].timestamp);
      }
    },
    addOrUpdateMessageToList(message) {
      if(!message || !message.room || !this.contact.room || message.room !== this.contact.room || !message.clientId && !message.msgId) {
        return;
      }

      if (this.windowFocused && !this.minimized) {
        chatWebSocket.setRoomMessagesAsRead(this.contact.room);
      }

      if(this.isScrollPositionAtEnd()) {
        this.setScrollToBottom();
      }

      const index = this.messages.findIndex(messageObj => messageObj.clientId && messageObj.clientId === message.clientId || messageObj.msgId && messageObj.msgId === message.msgId);
      if (index > -1) {
        if (!message.fullname) {
          message.fullname = this.messages[index].fullname;
        }
        this.messages.splice(index, 1, message);
      } else {
        this.messages.push(message);
      }
    },
    messageDeleted(e) {
      const messageObj = e.detail;
      const message = messageObj.data;
      this.unifyMessageFormat(messageObj, message);
      this.addOrUpdateMessageToList(message);
    },
    unifyMessageFormat(messageObj, message) {
      if(!message.room && messageObj.room) {
        message.room = messageObj.room;
      }
      if(!message.user && (messageObj.user || messageObj.sender)) {
        message.user = messageObj.user ? messageObj.user : messageObj.sender;
      }
    },
    editLastMessage() {
      if(!this.messages || !this.messages.length) {
        return;
      }
      let lastMessage = null;
      let index = this.messages.length -1;
      while(!lastMessage && index >= 0) {
        const message = this.messages[index];
        if(!message.isDeleted && message.type !== this.$constants.DELETED_MESSAGE && !message.isSystem && message.user === eXo.chat.userSettings.username) {
          lastMessage = message;
        }
        index--;
      }
      if (lastMessage) {
        this.editMessage(lastMessage);
      }
    },
    editMessage(message) {
      this.messageToEdit = JSON.parse(JSON.stringify(message));
      this.messageToEdit.msg = this.messageToEdit.msg || this.messageToEdit.message || '';
      this.messageToEdit.msg = this.messageToEdit.msg
        .replace(/&#92/g, '\\')
        .replace(/&lt;/g, '<')
        .replace(/&gt;/g, '>')
        .replace(/&quot;/g, '"')
        .replace(/<br( *)\/?>/g, '\n')
        .replace('&#38', '&');
      this.showEditMessageModal = true;
      this.$nextTick(() => {
        if(this.$refs.editMessageComposerArea) {
          this.$refs.editMessageComposerArea.focus();
        }
      });
    },
    saveMessage(event) {
      if (!event || event.keyCode === this.$constants.ENTER_CODE_KEY) {
        if (event && (event.ctrlKey || event.altKey || event.shiftKey)) {
          $(this.$refs.editMessageComposerArea).insertAtCaret('\n');
        } else {
          this.messageToEdit.msg = this.messageToEdit.msg.trim();
          this.messageModified(this.messageToEdit);
          this.showEditMessageModal = false;
        }
      }
    },
    closeModal() {
      this.showEditMessageModal = false;
    },
    searchMessage(e) {
      this.searchKeyword = e.detail.trim();
    },
    preventDefault(event) {
      if (event.keyCode === this.$constants.ENTER_CODE_KEY) {
        event.stopPropagation();
        event.preventDefault();
      }
    }
  }
};
</script>