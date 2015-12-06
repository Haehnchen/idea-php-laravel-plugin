<?php

namespace Illuminate\Foundation {
    class Application
    {
        public function registerCoreContainerAliases()
        {
            $aliases = [
                'foo' => 'Foo\Bar',
            ];
        }
    }
}

namespace Foo
{
    class Bar
    {
        public function bar() {}
    }
}

namespace Illuminate\Contracts\Container {
    class Container
    {
        public function make() {}
    }
}

namespace
{
    function app() {}

    class App
    {
        public static function make() {}
    }
}

