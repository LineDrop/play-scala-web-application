$(document).ready(function(){
    $("#button_request").click(function() { validate(); })
})

$(document).on('keypress', function(e) { if(e.which == 13) {validate();} })

function validate() {
    if ($('#email').val().indexOf("@") != -1) forgot();
    else $('#email').focus();
}

function forgot() {

    $.ajax({
        type: "POST",
        url: "/forgot/json",
        headers: {"CSRF-Token" : $("#csrf").val()},
        data: JSON.stringify({"email": $("#email").val()}),
        contentType: "application/json; charset=utf-8",
        dataType: "json"
    });

    $("#reset").addClass("d-none");
    $("#proceed").removeClass("d-none");

}