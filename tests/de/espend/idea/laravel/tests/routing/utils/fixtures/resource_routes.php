<?php

Route::resource('test1', 'FooController');

Route::group(['prefix' => 'foo', 'as' => 'foo::'], function () {
    Route::resource('test2', 'FooController');

    Route::group(['prefix' => 'bar', 'as' => 'bar::'], function () {
        Route::resource('test3', 'FooController');
    });
});

Route::resource('test4', 'FooController', [
    'only' => [
        'index', 'show'
    ]
]);

Route::resource('test5', 'FooController', [
    'except' => [
        'show', 'edit', 'update', 'destroy', 'wrong'
    ]
]);

Route::resource('test6', 'FooController', [
    'only' => [
        'index', 'show', 'wrong'
    ],
    'names' => [
        'index' => 'named.route',
        'wrong' => 'nothing',
    ]
]);