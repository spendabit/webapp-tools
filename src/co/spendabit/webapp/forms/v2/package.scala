package co.spendabit.webapp.forms

import co.spendabit.webapp.forms.controls.LabeledControl

package object v2 {

  sealed trait ValidationResult[T] { def isValid: Boolean }
  case class Valid[T](values: T)             extends ValidationResult[T] { val isValid = true }
  case class Invalid[T](errors: Seq[String]) extends ValidationResult[T] { val isValid = false }

  object Invalid {
    def apply[T](error: String): ValidationResult[T] = Invalid(Seq(error))
  }

  trait WebForm1[A] extends v2.BaseWebForm[A] {
    protected def fields: LabeledControl[A]
    protected def f1 = fields
    protected val fieldsSeq = Seq(f1)
    protected def seqToTuple(s: Seq[_]) = s.head.asInstanceOf[A]
    protected def widgetsHTML(values: Option[A]): Seq[xml.NodeSeq] = {
      val vs = values.map(vs => Some(vs)).getOrElse(None)
      Seq(f1.widgetHTML(vs))
    }
  }

  trait WebForm2[A, B] extends v2.BaseWebForm[(A, B)] {
    protected def fields: (LabeledControl[A], LabeledControl[B])
    protected def f1 = fields._1
    protected def f2 = fields._2
    protected val fieldsSeq = Seq(f1, f2)
    protected def seqToTuple(s: Seq[_]) = (s.head, s(1)).asInstanceOf[(A, B)]
    protected def widgetsHTML(values: Option[(A, B)]): Seq[xml.NodeSeq] = {
      val vs = values.map(vs => (Some(vs._1), Some(vs._2))).getOrElse((None, None))
      Seq(f1.widgetHTML(vs._1), f2.widgetHTML(vs._2))
    }
  }
}
