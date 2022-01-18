
# CRYPTOFINTECHX API

The API for the CRYPTOFINTECHX platform project

## API Reference

### Create a new user

```http
  GET /api/user/create
```

#### Request Headers

| Header | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `X-Api-Key` | `string` | **Required**. API key |

#### Request Body

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `email`      | `string` | **Required**. The email address of the new user |
| `country`      | `string` | **Required**. The country of the new user |
| `full_name`      | `string` | **Required**. The full name of the new user |
| `phone`      | `string` | The phone number of the new user |
| `password`      | `string` | **Required**. The password of the new user |
| `avatar_url`      | `string` | The url to the profile picture of the new user |
| `category`      | `enum` | **Required**. The investment category of the new user |
| `dob`      | `string` | **Required**. The date of birth of the new user |

Possible values for category = ['Normal', 'Silver', 'Gold', 'Diamond']

### Authenticate a user

```http
  POST /api/user/auth
```

#### Request Headers

| Header | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `X-Api-Key` | `string` | **Required**. API key |

#### Request Body

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `email`      | `string` | **Required**. Email address of the user to authenticate |
| `password`      | `string` | **Required**. The password of the user to authenticate |

### Change a user's password

```http
  POST /api/user/edit/password
```

#### Request Headers

| Header | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `X-Auth` | `string` | **Required**. User token issued at login |
| `X-Api-Key` | `string` | **Required**. API key |

#### Request Body

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `old_password`      | `string` | **Required**. The old password of the user |
| `new_password`      | `string` | **Required**. The new password to store |

### Edit a user's profile

```http
  PUT /api/user/edit/profile/${user_id}
```

#### Request Headers

| Header | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `X-Auth` | `string` | **Required**. User token issued at login |
| `X-Api-Key` | `string` | **Required**. API key |

#### Request Body

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `new_profile`      | `object` | **Required**. The new profile details |

### Delete a user's account

#### Request Headers

| Header | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `X-Auth` | `string` | **Required**. User token issued at login |
| `X-Api-Key` | `string` | **Required**. API key |

```http
  DELETE /api/user/delete/${user_id}
```

Deletes the user account associated with the ID `user_id`

## Appendix

Important information

- `DEV_URL`: [link](https://cryptofintechx-backend-dev.herokuapp.com)
- `PROD_URL`:  [link](https://5u35m7cyajmu4s57.cryptofintechx.com)
