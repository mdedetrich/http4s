package org.http4s

import scalaz.concurrent.Task

object MessageSyntax extends MessageSyntax

trait MessageSyntax {
  implicit def requestSyntax(req: Task[Request]): TaskMessageOps[Request] = new TaskRequestOps(req)

  implicit def responseSyntax(resp: Task[Response]): TaskResponseOps = new TaskResponseOps(resp)
}

trait TaskMessageOps[M <: Message] extends Any with MessageOps {
  type Self = Task[M#Self]

  def self: Task[M]

  /** Add a body to the message
    * @see [[Message]]
    */
  def withBody[T](b: T)(implicit w: Writable[T]): Self = self.flatMap(_.withBody(b)(w))

  /** Generates a new message object with the specified key/value pair appended to the [[org.http4s.AttributeMap]]
    *
    * @param key [[AttributeKey]] with which to associate the value
    * @param value value associated with the key
    * @tparam A type of the value to store
    * @return a new message object with the key/value pair appended
    */
  override def withAttribute[A](key: AttributeKey[A], value: A): Self = self.map(_.withAttribute(key, value))

  /** Replaces the [[Header]]s of the incoming Request object
    *
    * @param headers [[Headers]] containing the desired headers
    * @return a new Request object
    */
  override def withHeaders(headers: Headers): Self = self.map(_.withHeaders(headers))

  /** Add the provided headers to the existing headers, replacing those of the same header name */
  override def putHeaders(headers: Header*): Self = self.map(_.putHeaders(headers:_*))

  override def filterHeaders(f: (Header) => Boolean): Self = self.map(_.filterHeaders(f))
}

final class TaskRequestOps(val self: Task[Request]) extends AnyVal with TaskMessageOps[Request]

final class TaskResponseOps(val self: Task[Response]) extends AnyVal with TaskMessageOps[Response] with ResponseOps {
  /** Response specific extension methods */
  override def withStatus[S <% Status](status: S): Self = self.map(_.withStatus(status))
}
