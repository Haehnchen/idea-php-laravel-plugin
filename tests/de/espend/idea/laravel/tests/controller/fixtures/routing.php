<?php

namespace App\Http\Controllers
{
    interface Controller {}
}

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

namespace App\Http\Controllers
{
    class FooController implements \App\Http\Controllers\Controller
    {
        public function foo() {}
    }
}

namespace Foo\Controllers
{
    class BarController implements \App\Http\Controllers\Controller
    {
        public function foo() {}
    }
}