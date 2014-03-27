(ns vending.core)



(defn make-default-machine [] { :stock {:caramel-wafer 5 :teacake 5 :snowball 5}
	:coins {:merk 1 :plack 4 :bawbee 4 :bodle 4}
	:tendered nil
	:message ""
	:change nil
	:output nil} )

(def coin-values {:merk 100 :plack 25 :bawbee 10 :bodle 5})

(keys coin-values)

(.indexOf (keys coin-values) :bodle)

(def item-prices {:caramel-wafer 65 :teacake 100 :snowball 150})

(defn coin-value [coin]
	(coin-values coin))

(defn coins-value [coins]
  "Sum the value of these coins."
	(apply + (map coin-value coins)))


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
  a map {:merk 2 :plack 1 :bawbee 1 :bodle 1}. Optional second argument: an
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

(defn in-make-change [amount merk plack bawbee bodle]
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

(defn make-change [amount merk plack bawbee bodle]
  "Given this amount of change to make, and this number each of merks, placks, bawbees
  and bodles, return a tuple (merk plack bodle bawbee) which indicates the number remaining
	after making change, or nil if not possible"
	(map #(- %1 %2) (map #(or % 0) (list merk plack bawbee bodle))
		(apply in-make-change (map #(or % 0) (list amount merk plack bawbee bodle)))
		))


(defn subtract-change [coin-stacks change]
  "Return a copy of these coin-stacks with this change removed"
  {:merk (- (:merk coin-stacks) (nth change 0))
   :plack (- (:plack coin-stacks) (nth change 1))
   :bawbee (- (:bawbee coin-stacks) (nth change 2))
   :bodle (- (:bodle coin-stacks) (nth change 3))})

(defn subtract-change-machine [machine change]
  "return a copy of this machine which has this amount of change removed from its coins"
  (let [coins (:coins machine)]
    (assoc (dissoc machine :coins) :coins (subtract-change coins change))))

(defn make-change-machine [machine change]
  "given this machine and these numbers of coins to remove, return a copy of the machine
  with the coins removed"
    (let [tend (sum-coins (:tendered machine))]
        (cond (= change '(0 0 0 0)) machine)
          true (assoc (dissoc (subtract-change-machine machine change) :change ) :change change )))

(defn remove-from-stock [machine item]
  "return a copy of this machine with one fewer of this item in stock"
	(update-in machine [:items item] dec))

(defn deliver-item [machine item change]
	(make-change-machine
		(remove-from-stock
			(assoc (dissoc machine :output) :output (cons item (:output machine)))
			item)
    (item item-prices)
		change))


(defn get-item [machine item]
	(let [item-price (item item-prices)
		coins (:coins machine)
    tendered (coins-value (:tendered machine))
		change (make-change (- tendered item-price) (:merk coins) (:plack coins) (:bawbee coins) (:bodle coins))]
    (print change)
    (cond (>= 0 (item (:stock machine))) (message-machine (coin-return machine) (str "Sorry, " item " not in stock"))
      (<= tendered item-price) (message-machine machine "Please insert more money")
      (= change '(0 0 0 0)) (message-machine (coin-return machine) "Sorry, I don't have enough change.")
      true (message-machine (deliver-item machine item change) (str "Enjoy your " item)))))

(defn get-caramel-wafer [machine]
	(get-item machine :caramel-wafer))

;; (get-caramel-wafer (add-coin (add-coin (make-default-machine) :merk) :merk))


