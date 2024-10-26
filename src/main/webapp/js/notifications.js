function showMessage(message, isSuccess) {
    var messageDiv = document.getElementById('message');
    var messageText = document.getElementById('messageText');
    messageText.innerText = message;
    messageDiv.className = isSuccess ? 'success-message' : 'error-message';
    messageDiv.style.display = 'block';
    setTimeout(function () {
        messageDiv.style.display = 'none';
    }, 1500);
}

function closeMessage() {
    var messageDiv = document.getElementById('message');
    messageDiv.style.display = 'none';
}


function closeModal() {
    document.getElementById('notificationModal').style.display = 'none';
}

window.onclick = function(event) {
    var modal = document.getElementById('notificationModal');
    if (event.target == modal) {
        modal.style.display = 'none';
    }
}
function checkPendingRequest() {
    const hasPendingRequest = "#{userBean.hasPendingRequest()}";
    if (hasPendingRequest) {
        showMessage('Вы уже отправили заявку!', false);
        return false;
    } else {
        showMessage('Заявка отправлена!', true);
        return true;
    }
}
