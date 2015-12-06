<?php

namespace {
    Route::get('user/profile', ['as' => 'profile']);
}


namespace Illuminate\Routing {
    interface UrlGenerator {
        public function route();
    }
}

namespace Illuminate\Contracts\Routing {
    interface UrlGenerator {
        public function route();
    }
}

namespace Collective\Html {
    interface HtmlBuilder {
        public function linkRoute();
    }
}