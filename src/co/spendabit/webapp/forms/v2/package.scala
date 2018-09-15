package co.spendabit.webapp.forms

import co.spendabit.webapp.forms.controls.Field

package object v2 {

  sealed trait ValidationResult[T] {
    def isValid: Boolean
    def errors: Seq[String]
  }

  case class Valid[T](values: T) extends ValidationResult[T] {
    val isValid = true
    val errors = Seq()
  }

  case class Invalid[T](errors: Seq[String]) extends ValidationResult[T] {
    val isValid = false
  }

  object Invalid {
    def apply[T](error: String): ValidationResult[T] = Invalid(Seq(error))
  }

  trait WebForm1[A] extends v2.BaseWebForm[A] {
    protected def fields: Field[A]
    protected def f1 = fields
    protected def fieldsSeq = Seq(f1)
    protected def seqToTuple(s: Seq[_]) = s.head.asInstanceOf[A]
    protected def widgetsHTML(values: Option[A]): Seq[xml.NodeSeq] = {
      val vs = values.map(vs => Some(vs)).getOrElse(None)
      Seq(f1.widgetHTML(vs))
    }
  }
}
