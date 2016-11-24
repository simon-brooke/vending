# vending

A Clojure implementation of the Vending Machine kata, see http://code.joejag.com/coding-dojo-vending-machine/

NOTE! I am LEARNING. This is not an example of how to do it.

## Usage

The state of the machine is represented by a structure which is passed around between functions.

So you can make a machine with make-default-machine:

    vending.repl=> (make-default-machine)
    {:tendered nil, :output nil, :coins {:plack 4, :merk 1, :bawbee 4, :bodle 4}, :change nil, :stock {:teacake 5, :caramel-wafer 5, :snowball 5}, :message ""}

You can add coins to it with add-coin; as this is a Scots machine the coins you can add are merks, placks, bawbees and bodles.

    vending.repl=> (add-coin (add-coin *1 :merk) :bawbee)
    {:message "Added a :bawbee", :tendered (:bawbee :merk), :output nil, :coins {:plack 4, :merk 1, :bawbee 4, :bodle 4}, :change nil, :stock {:teacake 5, :caramel-wafer 5, :snowball 5}}

The machine serves good Scottish super-foods: caramel wafers, teacakes and snowballs. To get a teacake, try

    vending.repl=> (get-teacake *1)
    {:message "Enjoy your :teacake", :coins {:merk 2, :plack 4, :bawbee 4, :bodle 4}, :change (:bawbee), :output (:teacake), :stock {:teacake 4, :caramel-wafer 5, :snowball 5}}

There's very little error checking; if you try to add a dollar, I've no idea what will happen but it probably won't be good.

Finally if you add coins and decide you want them back, you can use coin-return:

    vending.repl=> (coin-return (add-coin (add-coin (make-default-machine) :merk) :plack))
    {:message "Coins returned", :output nil, :coins {:plack 4, :merk 1, :bawbee 4, :bodle 4}, :change (:plack :merk), :stock {:teacake 5, :caramel-wafer 5, :snowball 5}}

## Possible future development

My panned next phase for this is to make it drive an interactive web page, first entirely server-side, and then, if that works, client-side.

Update: the wep page now roughly works (but isn't yet pretty). To play with it, use:

    lein ring server

## License

Copyright © 2014 Simon Brooke, Ali King and others

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version, simply because that's the default the package template gave us.
