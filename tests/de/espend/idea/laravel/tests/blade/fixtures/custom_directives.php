<?php

namespace Illuminate\Support\Facades
{
    class Blade
    {

    }
}

namespace
{
    class Blade extends \Illuminate\Support\Facades\Blade
    {
        public static function directive($name, $handler)
        {
        }
    }

    Blade::directive('datetime', function ($expression) {
        return "<?php echo ($expression)->format('m/d/Y H:i'); ?>";
    });
}