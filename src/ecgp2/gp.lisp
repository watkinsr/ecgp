;; Lisp will constantly run, calling a battle everytime it has finished evolving everything - once that piece of java is finished executing, it'll evolve again

;; We will need multiple trees for each genome (onScannedRobot etc.)

(defstruct individual
  program 
  (fitness 0)
  (hits 0))

;; defvar's necessary to prevent warnings when using setf

(defvar *number-of-fitness-cases* :unbound
  "The number of fitness cases")

(defvar *max-depth-for-new-individuals* :unbound
  "The maximum depth for individuals of the initial
   random generation")

(defvar *max-depth-for-individuals-after-crossover* :unbound
  "The maximum depth of new individuals created by crossover")

(defvar *fitness-proportionate-reproduction-fraction* :unbound
  "The fraction of the population that will experience fitness
   proportionate reproduction (with reselection)
   during each generation")

(defvar *crossover-at-any-point-fraction* :unbound
  "The fraction of the population that will experience
   crossover at any point in the tree (including terminals)
   during each generation")

(defvar *crossover-at-function-point-fraction* :unbound
  "The fraction of the population that will experience
   crossover at a function (internal) point in the tree
   during each generation.")

(defvar *max-depth-for-new-subtrees-in-mutants* :unbound
  "The maximum depth of new subtrees created by mutation")

(defvar *method-of-selection* :unbound
  "The method of selecting individuals in the population, we shall use :tournament for robocode")

(defvar *method-of-generation* :unbound
  ":grow will be utilised for robocode")

(defvar *best-individual* :unbound
  "Best individual found during this run of gp cycle")

(defvar *best-run-generation* :unbound
  "The generation at which the best-of-run individual was found")

(defvar *t1* :unbound
  "Terminal set 1 (for normal events)")

(defvar *t2* :unbound
  "Terminal set 2 (for robot scan events)")

(defvar function-set :unbound
  "Set of functions for gp system")

(defvar terminal-set :unbound
  "Set of terminals for gp system")

(defvar heading :unbound
  "Angle robot is turned at")

(defvar x :unbound
  "Current x location of robot")

(defvar y :unbound
  "Current y location of robot")

(defvar max-x :unbound
  "Max X location inside Robocode space")

(defvar max-y :unbound
  "Max Y location inside Robocode space")

(defvar energy :unbound
  "Energy of robot")

(defvar *current-gen* :unbound
  "Current generation of gp loop")

(defvar population :unbound
  "Population for our gp system")

(defvar population-size :unbound
  "Size of populus")

(defvar ramped-half-and-half
  "Tree grow method")

(defvar minimum-depth-of-trees
  "Minimum depth we should grow our trees from")

(defvar random-integer :unbound)

(defvar argument-map :unbound)

(defvar max-generations :unbound)

(defvar population-size :unbound)

(defun run-robocode-gp ()
   (setq population-size 5)
   (setq max-generations 100)
   (format t "~%BEGIN GENETIC PROGRAMMING FOR ROBOCODE...~%")

   ;; Validity checks
  (assert (and (integerp max-generations)
               (not (minusp max-generations)))
          (max-generations)
          "Max-generations must be non-negative integer")

  (assert (and (integerp population-size)
               (plusp population-size))
          (population-size)
          "Size-of-population must be set to positive integer")
 
  ; Set best-run variables
  (setf *best-run-generation* 0)
  (setf *best-individual* nil)
  (setf *current-gen* 0)
 
  ; Set general parameters
  (define-parameters)
   
  ;; Set function set
  (define-function-params)

  ;; Set terminal-set
  (setq terminal-set (define-terminals))

  ;; Set our fitness function: Our fitness function is dependent on  ;; how much damage we do to the enemy robot and how many bullets   ;; we successfully dodge
  
  (setq fitness-function (set-fitness-function))

  ;; Create population, requires random builds of trees function
  (format t "Population size: ~S~%Function set: ~S~%Terminal set: ~S" population-size function-set terminal-set)

  (let ((population
	  (create-population population-size function-set terminal-set)
       ))

  (format t "Population: ~S" population)

  ;; Run generation cycle until max-gens hit
  (if (< *current-gen* max-generations)
	(progn
	  (setf *current-gen* (+ *current-gen* 1))
	  ))

  ;; Print results
(with-open-file (str "src/ecgp2/evolved_data/programs.txt"
		     :direction :output
		     :if-exists :append
		     :if-does-not-exist :create)
  (format str "~S" population))

  ;; Return population for debugging purposes
  (return-from run-robocode-gp population)
))

(defun set-fitness-function ()
  (format t "Setting fitness function...~%")
  ; Bullet amount fitness function (set a maximum, i.e. 70)
  ; Movement amount fitness function (set a maximum, i.e. 30)
)

(defun terminal-criterion (current-generation max-generations)
  (values (>= current-generation max-generations)))

(defun define-parameters ()
  (setf *max-depth-for-new-individuals* 8)
  (setf *max-depth-for-individuals-after-crossover* 17)
  (setf *fitness-proportitionate-reproduction-fraction* 0.1)
  (setf *crossover-at-any-point-fraction* 0.2)
  (setf *crossover-at-function-point-fraction* 0.7)
  (setf *max-depth-for-new-subtrees-in-mutants* 4)
  (setf *method-of-selection* :fitness-proportionate)
  (setf *method-of-generation* ramped-half-and-half)
)

(defun define-terminals ()
  (setq getHeading 1)
  (setq getX 2)
  (setq getY 3)
  (setq energy 4)
  (setq terminal-set '(getHeading getX getY energy))
)

(defun define-function-params ()
  (setq function-set '(+ - * %))
  (setq argument-map '(2 2 2 2))
)

(defun % (numerator denominator)
  "The Protected Division Function"
  (values (if (= 0 denominator) 1 (/ numerator denominator)))
)

(defun choose-from-terminal-set (terminal-set)
  "Chooses random terminal based on length of terminal set, => get random integer in range (1 - length(terminal-set))"
  (elt terminal-set (random (length terminal-set))))

(defvar *generation-0-uniquifier-table*
  (make-hash-table :test #'equal)
  "Used to guarantee that all generation 0 individuals are unique")


(defun create-population (
	population-size function-set terminal-set)
  "Creates population which contain individuals. The program slot of each individual gets initialised to a random program."
  
  ;; Debug for arguments being passed in...
  (format t "~%~%Inside create-population~%ARGS ARE:::~%~%Population-size: ~S~%Function-set: ~S~%Terminal-set: ~S" population-size function-set terminal-set)

  (let ((population (make-array population-size))
	(minimum-depth-of-trees 1)
	(full-cycle-p nil))

  (format t "~%Population: ~S~%Minimum Depth of trees: ~S~%Full-cycle-p: ~S~%" population minimum-depth-of-trees full-cycle-p)

   ;; Iterate through individuals by size of populus
   (do ((individual-index 0))
	((>= individual-index population-size))
	
  (format t "Individual-index: ~S~%" individual-index)

   ;; Something to do with setting full cycle predicate
      (when (zerop (mod individual-index
			(max 5 (- *max-depth-for-new-individuals*
				  minimum-depth-of-trees))))
	(setf full-cycle-p (not full-cycle-p)))
 
  (format t "Full-cycle-p: ~S~%" full-cycle-p)

   ;; Create our new program
      (let ((new-program
	      (create-individual-program function-set terminal-set *max-depth-for-new-individuals* t full-cycle-p)
	   ))
	
	(format t "~%New-program: ~S~%" new-program)

	 (setf (aref population individual-index)
		(make-individual :program new-program))
	   (incf individual-index)

	  
   ))


   (return-from create-population population )

   )
)

(defun create-individual-program
   	(function-set terminal-set
   	 allowable-depth top-node-p full-p)
"Creates a program recursively using the specified functions
and terminals. When we hit depth of zero, we add terminals. Top-node-p evaluates to true only when we are being called as the top node in the tree. This allows us to make sure that we always put a function at the top of the tree. Full-p indicates whether this 
individual is to be maximally bushy or not."
  
  (format t "~%~%Inside create-individual-program~%ARGS::~%Function-set: ~S~%Terminal-set: ~S~%Allowable-depth: ~S~%Top-node-p: ~S~%Full-p: ~S~%" function-set terminal-set allowable-depth top-node-p full-p)

  (cond ((<= allowable-depth 0)
    ;; We've reached maxdepth, so just pack a terminal
    (choose-from-terminal-set terminal-set))
     ((or full-p top-node-p)
      ;; We're a full tree or top node so grab func
      (let ((choice (random-integer (length function-set))))
	(format t "~%Got random int for choosing a func from func set~S~%" choice)
	(let ((func (nth choice function-set))
	      (number-of-arguments (nth choice argument-map)))
	  (format t "~%Got function: ~S~%Num-of-args: ~S~%" func number-of-arguments)
	  (cons func 
		(create-arguments-for-function number-of-arguments 		    function-set argument-map terminal-set (- allowable-depth 1) full-p)))))

  )
)

(defun create-arguments-for-function (number-of-arguments function-set argument-map terminal-set allowable-depth full-p)
    (format t "Populate tree ")
    (if (eq number-of-arguments 0)
      nil
      (cons (eval (create-individual-program function-set terminal-set allowable-depth nil full-p))
	    (create-arguments-for-function (- number-of-arguments 1) function-set argument-map terminal-set allowable-depth full-p)
      )
    )  
  )

;; Auxillary functions
(defun random-floating-point-number (n)
  "Returns a pseudo random floating-point number
                in range 0.0 <= number < n"
  (let ((random-number (random n)))
    ;; We subtract the randomly generated number from 1.0
    ;; before scaling so that we end up in the range
    ;; 0.0 <= x < 1.0, not 0.0 < x <= 1.0
    (* n (- 1.0d0 random-number))))


(defun random-integer (n)
  "Returns a pseudo-random integer in the range 0 ---> n-1."
  (let ((random-number (random-floating-point-number 1.0)))
    (floor (* n random-number))))

(defun void-function (param)
  (let* ((class (jclass "ecgp2.Main"))
	 (intclass (jclass "int"))
	 (method (jmethod class "add" intclass intclass))
	 (result (jcall method param 3 4 )))
    (format t "Result of calling addTwoNumbers: ~A~%" result)))

(run-robocode-gp)

