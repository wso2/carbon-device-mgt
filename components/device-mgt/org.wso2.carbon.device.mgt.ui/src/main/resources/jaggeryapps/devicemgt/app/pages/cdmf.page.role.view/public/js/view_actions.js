$("#actbar").hide();
$("#actnbtn").hide();
if ($('#actbar ul').children().length == 0) {
    $("#actbtn").hide();
} else {
    $("#actbtn").show();
}

$('#actbar').click(function() {
    event.stopPropagation();
})

$('#actToggleBtn').click(function() {
    event.stopPropagation();
    $("#actbar").slideToggle();
});



$(document).click(function() {
    $("#actbar").hide();
})