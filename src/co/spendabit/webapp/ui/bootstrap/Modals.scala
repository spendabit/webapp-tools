package co.spendabit.webapp.ui.bootstrap

trait Modals {

  /** For rendering a modal in typical form, as is given in the example in the
    * Bootstrap documentation.
    */
  protected def standardModal(id: String, headerContent: xml.NodeSeq, bodyContent: xml.NodeSeq) =
    <div id={ id } class="modal fade" tabindex="-1" role="dialog"
         aria-hidden="true" aria-labelledby={ id + "-label" }>
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header" id="reasons-to-sign-title">
            <button type="button" class="close" data-dismiss="modal"
                    aria-hidden="true">&times;</button>
            <div id={ id + "-label" }>{ headerContent }</div>
          </div>
          <div class="modal-body">
            { bodyContent }
          </div>
          <div class="modal-footer">
            <button class="btn btn-default" data-dismiss="modal">Dismiss</button>
          </div>
        </div>
      </div>
    </div>

  protected def modalActivationLink(modalID: String, href: String,
                                    content: xml.NodeSeq): xml.NodeSeq = {
    val linkID = s"link-for-$modalID"
    <a href={ href } id={ linkID } data-toggle="modal" data-target={ "#" + modalID }
       data-backdrop="true">{ content }</a>
    <script type="text/javascript">{
      /* To avoid the 'href' interfering with modal, when JavaScript is enabled. */
      "$(function() { $('#" + linkID + "').attr('href', ''); });"
    }</script>
  }
}
