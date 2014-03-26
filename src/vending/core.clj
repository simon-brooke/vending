(ns vending.core)



(defn make-default-machine [] { :stock {:apples 5 :oranges 5 :lemons 5}
	:coins {:merk 1 :plack 4 :bawbee 4 :bodle 4}
	:tendered nil
	:last-message ""
	:change nil
	:output nil} )

(def coin-values {:merk 1 :plack 0.25 :bawbee 0.10 :bodle 0.05})

(keys coin-values)

(.indexOf (keys coin-values) :bodle)

(def item-prices {:apples 0.65 :oranges 1 :lemons 1.5})

(defn coin-value [coin]
	(coin-values coin))

(defn coins-value [coins]
  "Sum the value of these coins."
	(apply + (map coin-value coins)))

(defn coin-return [machine]
  "Return all tendered coins in this machine."
	(assoc (dissoc machine :tendered) :change (:tendered machine)))

(defn add-coin [machine coin]
  "Add this coin to this machine."
	(assoc (dissoc machine :tendered) :tendered (cons coin (:tendered machine))))

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
	"Return a tuple (merk plack bodle bawbee) which indicates the number remaining
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
	(map #(- %1 %2) (list merk plack bawbee bodle)
		(in-make-change amount merk plack bawbee bodle)
		))

(defn subtract-change [machine change]
  "subtract this change from this machine")

(defn make-change-machine [machine item-price change]
    (let [
      tend (sum-coins (:tendered machine))
		change (make-change item-price (:merk tend) (:plack tend) (:bawbee tend) (:bodle tend))]
      (cond (nil? change) machine)
        true (assoc (dissoc (subtract-change machine change) :change ) :change change )))

(defn remove-from-stock [machine item]
	machine)

(defn deliver-item [machine item change]
	(make-change-machine
		(remove-from-stock
			(assoc (dissoc machine :output) :output (cons item (:output machine)))
			item)
    (item item-prices)
		change))


(defn get-item [machine item]
	(let [item-price (item item-prices)
		tend (sum-coins (:tendered machine))
		change (make-change item-price (:merk tend) (:plack tend) (:bawbee tend) (:bodle tend))]
	(cond (<= 0 (item (:stock machine))) (coin-return machine)
		(<= (coins-value (:tendered machine)) item-price) (coin-return machine)
		(empty? change) (coin-return)
		true (deliver-item machine item change))))

(defn get-apple [machine]
	(get-item machine :apples))


