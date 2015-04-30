(ns vending.core
  (:use clojure.set))



(defn make-default-machine [] { :stock {:caramel-wafer 5 :teacake 5 :snowball 5}
	:coins {:merk 1 :plack 4 :bawbee 4 :bodle 4}
	:tendered nil
	:message ""
	:change nil
	:output nil} )

;; Laterly a bodle was worth either one sixth or one eighth of an English penny, but apparently
;; it was two pence Scots. Other values are more certain; a merk was 13 shillings and sixpence.
(def coin-values {:merk 160 :bawbee 6 :plack 4 :bodle 2})

(keys coin-values)

(.indexOf (keys coin-values) :bodle)

(def item-prices {:caramel-wafer 10 :teacake 16 :snowball 22})

(defn coin-value [coin]
	(coin-values coin))

(defn coins-value [coins]
  "Sum the value of this list of coins."
  (cond coins	(apply + (map coin-value coins))
        true 0))


(defn message-machine [machine message]
  "Return a machine with this message"
  (assoc (dissoc machine :message) :message (.toString message)))

(defn coin-return [machine]
  "Return all tendered coins in this machine."
	(message-machine (assoc (dissoc machine :tendered) :change (:tendered machine)) "Coins returned"))

(defn add-coin [machine coin]
  "Add this coin to this machine."
	(message-machine (assoc (dissoc machine :tendered) :tendered (cons coin (:tendered machine))) (str "Added a " coin)))

(defn add-coins [machine coins]
  "Add these coins to this machine"
	(cond (empty? coins) machine
		true (add-coins (add-coin machine (first coins)) (rest coins))))

(defn magic-inc [maybenum]
  "A wrapper round inc which treats nil as zero."
	(cond (nil? maybenum) 1
		true (inc maybenum)))

(defn sum-coin [coin sums]
  "Adds this coin (assumed to be on of :merk, :plack, :bawbee, :bodle)
  to this map of sums."
	(update-in sums [coin] magic-inc))

(defn sum-coins
	"takes a list in the form (:merk :merk :bawbee :plack :bodle) and returns
  a wallet {:merk 2 :plack 1 :bawbee 1 :bodle 1}. Optional second argument: an
  existing map in that general form."
	([coins]
		(sum-coins coins {}))
	([coins sums]
		(cond (empty? coins) sums
			true (sum-coins (rest coins) (sum-coin (first coins) sums)))))



(defn subtract-denomination [list position]
  "given a list of four numbers and a position, return a similar list with one subtracted
  from the number at this position in the list"
	(cond (= (count list) position)(cons (- (first list) 1) (rest list))
		true (cons (first list) (subtract-denomination (rest list) position))))

(defn subtract-nickle [list]
	(subtract-denomination list 1))

(defn subtract-bawbee [list]
	(subtract-denomination list 2))

(defn subtract-plack [list]
	(subtract-denomination list 3))

(defn subtract-merk [list]
	(subtract-denomination list 4))

(defn- in-make-change [amount merk plack bawbee bodle]
  "Given this amount of change to make, and this number each of merks, placks, bawbees
  and bodles, return a tuple (merk plack bodle bawbee) which indicates the number remaining
	after making change, or nil if not possible"
	(cond
		(= amount 0) (list merk plack bawbee bodle)
		(and (>= amount (:merk coin-values)) (> merk 0))
			(in-make-change (- amount (:merk coin-values)) (- merk 1) plack bawbee bodle)
		(and (>= amount (:plack coin-values)) (> plack 0))
			(in-make-change (- amount (:plack coin-values)) merk (- plack 1) bawbee bodle)
		(and (>= amount (:bawbee coin-values)) (> bawbee 0))
			(in-make-change (- amount (:bawbee coin-values)) merk plack (- bawbee 1) bodle)
		(and (>= amount (:bodle coin-values)) (> bodle 0))
			(in-make-change (- amount (:bodle coin-values)) merk plack bawbee (- bodle 1))))

(defn n-of [elt n]
  "return a list of n instances of elt"
  (cond (<= n 0) nil
        true (cons elt (n-of elt (dec n)))))

(defn to-coins [quadtuple]
  "Given a list in the form (merks placks bawbies bodles), return a
  flat list of coin symbols"
  (remove nil?
   (flatten
     (list
      (n-of :merk (nth quadtuple 0))
      (n-of :plack (nth quadtuple 1))
      (n-of :bawbee (nth quadtuple 2))
      (n-of :bodle (nth quadtuple 3))
      ))))

(defn make-change [amount coins]
  "Given this amount of change to make, and this number each of merks, placks, bawbees
  and bodles, return a wallet (a map with keys (:merk :plack :bawbee :bodle)) which indicates the
  number of each remaining after making change, or nil if not possible"
  (to-coins
    (let [merk (:merk coins)
          plack (:plack coins)
          bawbee (:bawbee coins)
          bodle (:bodle coins)]
      (map #(- %1 %2) (map #(or % 0) (list merk plack bawbee bodle))
        (apply in-make-change (map #(or % 0) (list amount merk plack bawbee bodle)))
        ))))


(defn subtract-change [coin-stacks change]
  "Return a copy of these coin-stacks with this change removed"
  (let [change-map (sum-coins change)]
    {:merk (- (:merk coin-stacks) (or (:merk change-map) 0))
     :plack (- (:plack coin-stacks) (or (:plack change-map) 0))
     :bawbee (- (:bawbee coin-stacks) (or (:bawbee change-map) 0))
     :bodle (- (:bodle coin-stacks) (or (:bodle change-map) 0))}))


(defn in-op-maps [op kys map1 map2 dflt]
  (cond (empty? kys) {}
        true
         (let [ky (first kys)]
           (assoc (in-op-maps op (rest kys) map1 map2 dflt)
             ky
             (apply op (list (or (map1 ky) dflt) (or (map2 ky) dflt)))))))


(defn op-maps [op map1 map2 dflt]
  "return a new map composed by applying this op to the values of
  the keys of these maps, using this default when no value present"
  (in-op-maps op (union (keys map1) (keys map2)) map1 map2 dflt))

(defn add-wallets [wallet1 wallet2]
  (op-maps + wallet1 wallet2 0))

(defn subtract-change-machine [machine change]
  "return a copy of this machine which has this amount of change removed from its coins"
  (let [coins (:coins machine)]
    (assoc (dissoc machine :coins) :coins (subtract-change coins change))))

(defn make-change-machine [machine change]
  "given this machine and these numbers of coins to remove, return a copy of the machine
  with the coins removed"
  (cond (empty? change) machine
    true (assoc (dissoc (subtract-change-machine machine change) :change ) :change change )))

(defn remove-from-stock [machine item]
  "return a copy of this machine with one fewer of this item in stock"
	(update-in machine [:stock item] dec))

(defn deliver-item [machine item]
  "Remove an item matching this item from stock and add it to the output hopper"
		(remove-from-stock
			(assoc (dissoc machine :output) :output (cons item (:output machine)))
			item))

(defn store-coins-machine [machine]
  ;; add the tendered coins to the coin stacks
  (let [wallet (sum-coins (:tendered machine))]
    (assoc
      (dissoc (dissoc machine :tendered) :coins)
      :coins (add-wallets wallet (:coins machine)))))

(defn get-item [machine item]
	(let [item-price (item item-prices)
		coins (:coins machine)
    tendered (coins-value (:tendered machine))
		change (make-change (- tendered item-price) coins)]
;;    (print (list "hello" item-price coins tendered change ))
    (cond (> 0 (item (:stock machine))) (message-machine (coin-return machine) (str "Sorry, " item " not in stock"))
      (<= tendered item-price) (message-machine machine "Please insert more money")
      (= change '(0 0 0 0)) (message-machine (coin-return machine) "Sorry, I don't have enough change.")
      true (message-machine
            (store-coins-machine
              (make-change-machine
               (deliver-item machine item) change))
            (str "Enjoy your " item)))))

(defn get-caramel-wafer [machine]
	(get-item machine :caramel-wafer))

(defn get-teacake [machine]
	(get-item machine :teacake))

(defn get-snowball [machine]
	(get-item machine :snowball))

;; (get-caramel-wafer (add-coin (add-coin (make-default-machine) :merk) :merk))


