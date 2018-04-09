<?php

Route::get('user/profile', ['as' => 'profile1']);

Route::get('user/profile', ['as' => 'profile2', function () {
}]);

Route::get('user/profile', [
    'as' => 'profile3', 'uses' => 'UserController@showProfile'
]);

Route::get('user/profile', 'UserController@showProfile')->name('profile4');

Foo::get('user/profile', 'UserController@showProfile')->name('foo');
Foo::get('user/profile', ['as' => 'bar']);