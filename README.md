# java-explore-with-me

ExploreWithMe — сервис для публикации событий и поиска компании.

## Дополнительная функциональность: комментарии к событиям

- Авторизованные пользователи оставляют комментарии к опубликованным событиям, могут редактировать и удалять их.
- После модерации администратора (публикация или отклонение) комментарии становятся доступны в публичном API.
- Повторное редактирование возвращает комментарий на модерацию.
- Ссылка на пул-реквест: 

### Эндпоинты

- **Публичный**: `GET /events/{eventId}/comments`
- **Пользовательский**: `POST /users/{userId}/events/{eventId}/comments`, `GET /users/{userId}/comments`, `PATCH /users/{userId}/comments/{commentId}`, `DELETE /users/{userId}/comments/{commentId}`
- **Административный**: `GET /admin/comments`, `PATCH /admin/comments/{commentId}`, `DELETE /admin/comments/{commentId}`

### Postman-тесты

Коллекция для быстрой проверки сценариев: `postman/feature-comments.postman_collection.json`. Перед запуском настройте переменные `baseUrl`, `userId` и `eventId`.
