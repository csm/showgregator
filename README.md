Quickstart:

1. Install cassandra and redis.

    $ sbt
    > project service
    > ~ ;reStop ;compile; reStart

Then, go to <http://localhost:7070>.

Here's the idea:

1. Start with shitty venue websites that list music performances. Build up a database of venues and their websites.
2. Parse these with HTML, plus some simple smarts about how to look for the relevant information.
    * If some sites are shittier than others, add custom parsing.
3. Aggregate these into a big database.
4. Provide a clean front-end that lets you view web/iCal/RSS events, query by location, date, etc.
5. Let users sign up, create their own calendars, calendars shared among groups of friends, etc.
    * Participants in a calendar can view events added (as iCal or RSS too), add events (subject to creator's permissions), comment on events, upvote/downvote events.
    * To participate in a calendar, you don't need an account, just an email address (though we would encourage creating accounts).
6. Public calendars, by region.
    * Includes public events within that region.

## Parts

* showgregator-daemon -- scrapes configured sites for show information; pushes that to the service. Scala.
* showgregator-service -- JSON REST service that handles input from the daemon, serves up user data to the frontend. Scala.
* showgregator-web -- AngularJS front end, calls the service for data.

License: GNU Affero General Public License v3.