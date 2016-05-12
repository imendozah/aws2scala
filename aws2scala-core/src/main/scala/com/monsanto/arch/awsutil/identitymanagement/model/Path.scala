package com.monsanto.arch.awsutil.identitymanagement.model

/** Represents a path within an ARN.
  *
  * @param elements the individual path elements
  */
case class Path(elements: Seq[String]) {
  /** Returns a string representing the path. */
  val pathString = if (elements.isEmpty) "/" else elements.mkString("/", "/", "/")
}

object Path {
  /** Constant for an empty path. */
  val empty: Path = Path(Seq.empty)

  /** Extractor for getting a `Path` from a string. */
  object fromString {
    def unapply(str: String): Option[Path] = {
      str.split("/").toList match {
        case Nil        ⇒ Some(Path.empty)
        case "" :: rest ⇒ Some(Path(rest))
        case _          ⇒ None
      }
    }
  }
}
