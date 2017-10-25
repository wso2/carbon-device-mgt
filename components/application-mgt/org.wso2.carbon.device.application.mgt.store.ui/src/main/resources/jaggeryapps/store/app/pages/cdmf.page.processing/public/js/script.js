var pollingCount = 24;
function poll() {
    $.ajax({
               url: context + "/api/user/environment-loaded",
               type: "GET",
               success: function (data) {
                   if (data.isLoaded) {
                       window.location = context + "/";
                   }
               },
               dataType: "json",
               complete: setTimeout(function () {
                   pollingCount = pollingCount - 1;
                   if (pollingCount > 0) {
                       poll();
                   } else {
                       $(".loading-animation .logo").hide();
                       $(".loading-animation").prepend(
                           '<i class="fw fw-error fw-inverse fw-2x" style="float: left;"></i>');
                       $(".loading-animation p").css("width", "150%")
                           .html("Ops... it seems something went wrong.<br/> Refresh the page to retry!");
                   }
               }, 5000),
               timeout: 5000
           });
}

$(document).ready(function () {
    poll();
});