package org.showgregator.service.mail

/*
Quick outline:

- SES for email sending. We will likely be on AWS, it's probably cheapest.
- Need self-rate-limiting. When we send mail to an address, throw that address in a key in redis.
- If that key is there, don't send email.
- Key has a TTL, once it expires we can send mail again.
- Figure out how to mustache template it.
 */

// TODO
trait Mailer {
  def sendConfirmEmail()
}
