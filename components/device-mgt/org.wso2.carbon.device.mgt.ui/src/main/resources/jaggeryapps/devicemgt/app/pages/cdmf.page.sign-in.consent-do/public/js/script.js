function approved() {
    var mandatoryClaimCBs = $(".mandatory-claim");
    var checkedMandatoryClaimCBs = $(".mandatory-claim:checked");

    if (checkedMandatoryClaimCBs.length == mandatoryClaimCBs.length) {
        document.getElementById('consent').value = "approve";
        document.getElementById("consentForm").submit();
    } else {
        $("#modal_claim_validation").modal();
    }
}

function deny() {
    document.getElementById('consent').value = "deny";
    document.getElementById("consentForm").submit();
}

$(document).ready(function () {
    $("#consent_select_all").click(function () {
        if (this.checked) {
            $('.checkbox input:checkbox').each(function () {
                $(this).prop("checked", true);
            });
        } else {
            $('.checkbox :checkbox').each(function () {
                $(this).prop("checked", false);
            });
        }
    });
    $(".checkbox input").click(function (e) {
        if (e.target.id !== 'consent_select_all') {
            $("#consent_select_all").prop("checked", false);
        }
    });
});