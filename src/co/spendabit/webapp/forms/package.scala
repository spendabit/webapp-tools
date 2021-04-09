package co.spendabit.webapp

package object forms {

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
}
