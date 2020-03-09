$(document).ready(function(){
    $("#button_add").click(function() { validate_add_user(); })
    $("#button_save").click(function() { validate_edit_user(); })
    $("#button_delete").click(function() { delete_user(); })

    $("#add_user_modal").on('hidden.bs.modal',function(){ window.location.replace("/users"); })
    $("#edit_user_modal").on('hidden.bs.modal',function(){ window.location.replace("/users"); })
})

$(document).on('keypress', function(e) { if(e.which == 13) {validate_add_user();} })

function validate_add_user() {
    var valid = true;

    if ( $('#add_email').val().indexOf("@") == -1 ) {
        $('#add_email').focus();
        valid = false;
    }

    if ( $('#add_name').val().length < 1 && $('#add_name').val() != 'Name' ) {
            $('#add_name').focus();
            valid = false;
    }

    if(valid == true) add_user();
}

function validate_edit_user() {
    var valid = true;

    if ( $('#edit_name').val().length < 1 && $('#edit_name').val() != 'Name' ) {
        $('#edit_name').focus();
        valid = false;
    }

    if(valid == true) edit_user();
}


function add_user() {

    console.log('Adding user')

    $.ajax({
        type: "POST",
        url: "/user/add/json",
        headers: {"CSRF-Token" : $("#csrf").val()},
        data: JSON.stringify({"name": $("#add_name").val(),"email": $("#add_email").val(),"role": $("#add_role").val()}),
        contentType: "application/json; charset=utf-8",
        success: function(obj) {
                if (obj.status == 'success') {
                    $("#add_name").addClass("d-none");
                    $("#add_email").addClass("d-none");
                    $("#add_role").addClass("d-none");
                    $("#button_add").addClass("d-none");
                    $("#prompt").html('<div class="alert alert-success"><p class="mb-0">' + obj.message + '</p></div>');
                }
                else {
                    $("#prompt").html('<div class="alert alert-warning"><p class="mb-0">' + obj.message + '</p></div>');
                }
            },
        dataType: "json"
    });

}

function show_edit_user_modal(name, email, role) {

    $("#edit_name").val(name);
    $("#edit_email").val(email);
    $("#edit_role").val(role);

    $("#edit_user_modal").modal('show');
}

function edit_user() {

    console.log('Editing user')

    $.ajax({
        type: "POST",
        url: "/user/edit/json",
        headers: {"CSRF-Token" : $("#csrf").val()},
        data: JSON.stringify({"name": $("#edit_name").val(),"email": $("#edit_email").val(),"role": $("#edit_role").val()}),
        contentType: "application/json; charset=utf-8",
        success: function(obj) {
                if (obj.status == 'success') {
                    $("#edit_name").addClass("d-none");
                    $("#edit_email").addClass("d-none");
                    $("#edit_role").addClass("d-none");
                    $("#button_save").addClass("d-none");
                    $("#button_delete").addClass("d-none");
                    $("#edit_user_prompt").html('<div class="alert alert-success"><p class="mb-0">' + obj.message + '</p></div>');
                }
                else {
                    $("#edit_user_prompt").html('<div class="alert alert-warning"><p class="mb-0">' + obj.message + '</p></div>');
                }
            },
        dataType: "json"
    });

}

function delete_user() {

    console.log('Deleting user')

    $.ajax({
        type: "POST",
        url: "/user/delete/json",
        headers: {"CSRF-Token" : $("#csrf").val()},
        data: JSON.stringify({"name": $("#edit_name").val(),"email": $("#edit_email").val(),"role": $("#edit_role").val()}),
        contentType: "application/json; charset=utf-8",
        success: function(obj) {
                if (obj.status == 'success') {
                    $("#edit_name").addClass("d-none");
                    $("#edit_email").addClass("d-none");
                    $("#edit_role").addClass("d-none");
                    $("#button_save").addClass("d-none");
                    $("#button_delete").addClass("d-none");
                    $("#edit_user_prompt").html('<div class="alert alert-success"><p class="mb-0">' + obj.message + '</p></div>');
                }
                else {
                    $("#edit_user_prompt").html('<div class="alert alert-warning"><p class="mb-0">' + obj.message + '</p></div>');
                }
            },
        dataType: "json"
    });

}