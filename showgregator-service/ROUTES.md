* `/` -- main splash page; login and signup links
    * `/login` -- login page, POST logs in, sends to user home.
    * `/signup` -- form to register an account.
    * `/calendar/<id>` -- view a calendar when logged in (params: month and year)
    * `/calendar/<id>/access/<key>` -- view a calendar as a transient user (email sharing, no account)
    * `/event/<id>` -- view details for an event
    * `/rest/api/v1` -- REST API endpoints
        * `/rest/api/v1/events`