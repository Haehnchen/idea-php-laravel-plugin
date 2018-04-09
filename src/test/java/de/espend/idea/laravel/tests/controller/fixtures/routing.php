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
        public static function controller($uri, $controller);
        public static function resource($uri, $controller, $options);
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

        public static function controller($uri, $controller)
        {
            return \Illuminate\Routing\Router::controller($uri, $controller);
        }

        public static function resource($uri, $controller, $options = [])
        {
            return \Illuminate\Routing\Router::resource($uri, $controller, $options);
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

namespace App\Http\Controllers\Group
{
    class GroupController implements \App\Http\Controllers\Controller
    {
        public function foo() {}
    }
}

namespace Foo\Controllers
{

    use Foo\Bar\Foo;

    class BarController implements \App\Http\Controllers\Controller
    {
        use Foo;

        public function foo() {}
    }
}

namespace Foo\Bar
{
    trait Foo
    {
        public function auth(\DateTime $foo) {}
    }
}
