package co.spendabit.webapp

import co.spendabit.webapp.forms.controls.LabeledControl

package object forms {

  sealed trait ValidationResult[T] { def isValid: Boolean }
  case class Valid[T](values: T)             extends ValidationResult[T] { val isValid = true }
  case class Invalid[T](errors: Seq[String]) extends ValidationResult[T] { val isValid = false }

  object Invalid {
    def apply[T](error: String): ValidationResult[T] = Invalid(Seq(error))
  }

  case class WebForm2[A, B](f1: LabeledControl[A], f2: LabeledControl[B]) extends BaseWebForm[(A, B)] {
    protected val fields = Seq(f1, f2)
    protected def seqToTuple(s: Seq[_]) = (s.head, s(1)).asInstanceOf[(A, B)]
    protected def widgetsHTML(values: Option[(A, B)]) = {
      val vs = values.map(vs => (Some(vs._1), Some(vs._2))).getOrElse((None, None))
      Seq(f1.widgetHTML(vs._1), f2.widgetHTML(vs._2))
    }
  }

  case class WebForm3[A, B, C](f1: LabeledControl[A], f2: LabeledControl[B], f3: LabeledControl[C])
          extends BaseWebForm[(A, B, C)] {
    protected val fields = Seq(f1, f2, f3)
    protected def seqToTuple(s: Seq[_]) = (s.head, s(1), s(2)).asInstanceOf[(A, B, C)]
    protected def widgetsHTML(values: Option[(A, B, C)]) = {
      val vs = values.map(vs => (Some(vs._1), Some(vs._2), Some(vs._3))).
        getOrElse((None, None, None))
      Seq(f1.widgetHTML(vs._1), f2.widgetHTML(vs._2), f3.widgetHTML(vs._3))
    }
  }

//  case class WebForm5[A, B, C, D, E](f1: LabeledControl[A], f2: LabeledControl[B], f3: LabeledControl[C],
//                                     f4: LabeledControl[D], f5: LabeledControl[E])
//          extends BaseWebForm[(A, B, C, D, E)] {
//    protected val fields = Seq(f1, f2, f3, f4, f5)
//    protected def seqToTuple(s: Seq[_]) =
//      (s(0), s(1), s(2), s(3), s(4)).asInstanceOf[(A, B, C, D, E)]
//  }
}
