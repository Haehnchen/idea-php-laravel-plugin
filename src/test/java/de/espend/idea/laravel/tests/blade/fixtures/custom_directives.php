<?php

namespace Illuminate\Support\Facades
{
    class Blade
    {
        public static function directive($name, $handler) {}
    }
}

namespace
{

    use Illuminate\Support\Facades\Blade as Foo;

    class Blade extends \Illuminate\Support\Facades\Blade
    {
        public static function directive($name, $handler)
        {
        }
    }

    Blade::directive('datetime', function ($expression) {
        return "<?php echo ($expression)->format('m/d/Y H:i'); ?>";
    });

    Foo::directive('foo', function($expression) {});
}
