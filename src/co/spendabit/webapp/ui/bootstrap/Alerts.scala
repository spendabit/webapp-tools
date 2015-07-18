package co.spendabit.webapp.ui.bootstrap

trait Alerts extends Glyphicons {

  sealed trait AlertContext { def slug: String }
  case object SuccessAlert extends AlertContext { val slug = "success" }
  case object InfoAlert    extends AlertContext { val slug = "info"    }
  case object WarningAlert extends AlertContext { val slug = "warning" }
  case object DangerAlert  extends AlertContext { val slug = "danger"  }

  protected def alert(context: AlertContext, msg: xml.NodeSeq) =
    <div class={ s"alert alert-${context.slug}" } role="alert">{ msg }</div>

  /** Generates an alert having a glyphicon on the left-hand side.
    */
  protected def glyphAlert(context: AlertContext, glyph: String, text: xml.NodeSeq) =
    <div class={ s"alert alert-${context.slug} alert-glyphicon" }>
      <div class="row">
        <div class="col-xs-2 col-sm-1 icon">
          { glyphicon(glyph) }
        </div>
        <div class="col-xs-10 col-sm-11 text">
          { text }
        </div>
      </div>
    </div>
}
