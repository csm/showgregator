Data model (WIP)

* Calendar
    * Unique ID
    * Public/private
    * Name
    * ACL
* Venue
    * Unique ID
    * Name
    * Address
    * Geolocation
    * Tags (key/value pairs)
    * Public/private
    * ACL
* Events
    * Unique ID
    * Name
    * Date and time
    * Venue ID

## User Flow

* Register token (link to register)
    * Register creates pending user, deletes register token (confirm email sent). Has TTL.
        * Confirming email creates user, deletes pending user.
* Transient user (invited via other user)
    * Register creates pending user (confirm email sent). Has TTL.
        * Confirming email creates user, deletes pending user, transient user.