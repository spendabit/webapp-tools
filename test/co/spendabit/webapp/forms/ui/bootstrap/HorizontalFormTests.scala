package co.spendabit.webapp.forms.ui.bootstrap

import co.spendabit.XMLHelpers
import co.spendabit.webapp.forms.controls._
import co.spendabit.webapp.forms.v2.{BaseWebForm, WebForm1}
import org.scalatest.funsuite.AnyFunSuite

class HorizontalFormTests extends AnyFunSuite with XMLHelpers {

  test("column widths are properly calculated/rendered") {

    class Renderer extends HorizontalForm {
      override protected val leftColumnWidth: Int = 3
    }

    val f = new BaseWebForm[String] with WebForm1[String] {
      val action = "/go"
      val method = POST
      val fields = TextInput(label = "Color", name = "color")
    }

    val markup = f.html(new Renderer)

    val form = markup \\ "form"
    val formGroup = form.head.child.filter(_.isInstanceOf[xml.Elem]).head
    val Seq(column1, column2): Seq[xml.Node] = formGroup.child.filter(_.isInstanceOf[xml.Elem])

    assert(getAttr(column1, "class").get.contains("col-sm-3"))
    assert(getAttr(column2, "class").get.contains("col-sm-9"))
  }
}