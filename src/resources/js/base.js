function handleUpdates(data){
  if('updates' in data){
    $.each(data['updates'], function(key, value){
      $('#' + key).html(value);
    });
  }
}

function loginCallback(data, textStatus, jqXHR){
  handleUpdates(data);
  if(data.status == "failed"){
    $('#login-overlay').show();
  }
}

function loginUser(){
  var loc = window.location;
  var username = $('#login-overlay #username-entry')[0].value;
  var password = $('#login-overlay #password-entry')[0].value;
  $.ajax("/user/login", {'type': 'POST', 
                    'dataType': 'json',
                    'data' : {'username' : username, 'password' : password},
                    'success': loginCallback});
}

function logoutUser(){
  var loc = window.location;
  $.ajax("/user/logout", {'dataType': 'json', 'success': loginCallback});
}

function postCallback(data, textStatus, xhrReq){
    $('#post-edit-title')[0].value = '';
    $('#post-edit-body')[0].value = '';
    $('#add-post-message').html("Message Posted");
}

function submitPost(){
  var title = $('#post-edit-title')[0].value;
  var body = $('#post-edit-body')[0].value;
  $.ajax('/blog/post', {'type' : 'POST',
                      'dataType': 'json',
                      'data' : {'title' : title, 'body' : body},
                      'success' : postCallback});
}
