<?php

namespace Illuminate\Routing
{
    interface Router
    {
        public static function get($uri, $action);
        public static function group($attributes, $callback);
    }
}

namespace
{
    class Route
    {
        public static function get($uri, $action)
        {
            return \Illuminate\Routing\Router::get($uri, $action);
        }

        public static function group($attributes, $callback)
        {
            return \Illuminate\Routing\Router::group($attributes, $callback);
        }
    }
}

namespace
{
    Route::group(['as' => 'foo::'], function () {
        Route::get('bar', ['as' => 'bar']);

        Route::group(['as' => 'bar::'], function () {
            Route::get('test', ['as' => 'test']);
        });
    });
}