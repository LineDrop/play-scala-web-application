$(document).ready(function(){
    $("#button_sign_up").click(function() { validate(); })
})

$(document).on('keypress', function(e) { if(e.which == 13) {validate();} })

function validate() {
    if ($('#subscriber_email').val().indexOf("@") != -1) subscribe();
    else $('#subscriber_email').focus();
}

function subscribe() {

    $.ajax({
        type: "POST",
        url: "/subscribe/json",
        headers: {"CSRF-Token" : $("#csrf").val()},
        data: JSON.stringify({"email": $("#subscriber_email").val()}),
        contentType: "application/json; charset=utf-8",
        success: function(obj) {
            console.log( obj.message );
        },
        dataType: "json"
    });

    $("#thank_you").removeClass("d-none");
    $("#get_notified").addClass("d-none");
    $("#subscriber_email").addClass("d-none");
    $("#button_sign_up").addClass("d-none");
}