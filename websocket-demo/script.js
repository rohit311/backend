const socket = io("http://localhost:3000");
const messageForm = document.getElementById("send-container");
const messageContainer = document.getElementById('message-container');
const messageInput = document.getElementById("message-input");

const userName = prompt('What is your name?');
appendMessage('You joined');
socket.emit('new-user', userName);

socket.on('chat-message', data => {
  appendMessage(data);
});

socket.on('user-connected', name => {
  appendMessage(`${name} connected`);
});

messageForm.addEventListener("submit", e => {
  e.preventDefault();

  const message = messageInput.value;
  socket.emit('send-chat-message', message);
  messageInput.value = '';
});

function appendMessage(message) {
  const messageElement = document.createElement('div');
  messageElement.innerText = message;
  messageContainer.append(messageElement);
}