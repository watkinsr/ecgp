#|
ROYALTY-FREE LICENSE FOR GENETIC PROGRAMMING SOFTWARE FOR ACADEMIC PURPOSES
===========================================================================

As you requested, I am including herewith, FOR YOUR INFORMATION AND
INSPECTION ONLY, a copy of my "SIMPLE LISP" software for genetic
programming.  Please be advised that this software is copyrighted and
is the subject of my United States patents 4,935,877 5,136,686, and
5,148,513, foreign counterparts, and other patents pending.  

IF YOU WISH TO USE THIS SOFTWARE, you may have a royalty-free,
non-exclusive license under these proprietary rights, without the
right of sublicense, to use (but not to make or sell) the software
for academic purposes only and only then if you receive no money
or other valuable consideration as a result of its use.
Neither the software (or copies thereof) nor this license is
transferrable for any reason.  This license grants you the right
to make copies of the software which are necessary for your use,
and your use only, provided each copy incorporates this license.

The programs, procedures, and applications presented in this software
have been included for their instructional value.  The publisher and
author offer NO WARRANTY OF FITNESS OR MERCHANTABILITY for any
particular purpose or accept any liability with respect
to these programs, procedures, and applications.

If you wish such a licence under these terms, please indicate so
by signing and dating a copy of this notice and returning it to me
at John Koza, Box K, Los Altos, California 94023 USA.

I would like the above license and hereby agree to the terms set forth above.

LICENSEE:

NAME (Print) ____________________________

Signature _________________________________

Date _______________

PHYSICAL ADDRESS _______________________________

CITY, STATE, ZIP _______________________________

COUNTRY _____________________________

E-MAIL ADDRESS _______________________________________

PHONE ___________________________

|#
;;; The following is a pure (CLtL2) Common Lisp implementation
;;; of the Genetic Programming Paradigm.  Great care has been
;;; taken to ensure that the code below both works and is identical
;;; to the code shown in the book.  The exception to this is the
;;; set of "top-level" forms that are shown in the appendix as
;;; examples, for instance, of calls that would fire off the GPP.
;;; These have not been included in-line in the code so as to
;;; prevent execution of the system during the compile/load
;;; cycle.  All of these test expressions haven been included
;;; in one test function, however, which is discussed below.

;;; If you are interested in genetic programming, you may want
;;; to subscribe to the genetic programming mailing list. 
;;; The list is called genetic-programming@cs.stanford.edu
;;; To subscribe to it, you should send mail to
;;; genetic-programming-request@cs.stanford.edu 
;;;                    ^^^^^^^^ 
;;; clearly giving the address you with to have mail sent to.

;;; The code is split into a number of parts, each delimited
;;; by the comment line:

;;;===================================================


;;; 1) The first component is the Kernel of the Genetic
;;; Programming implementation.  This is the domain-independent
;;; code which can be used to execute a variety of different
;;; problems, three of which are provided.

;;; 2) The second component concerns optimizations for the evaluation
;;; of individuals.  These include the function Fast-Eval, which
;;; can be used in your fitness functions to speed things up.
;;; Also shown are the "pseudo-macro"s shown in the book.
;;; The instalation of this form of optimization is not portable
;;; between different Common Lisp implementations.
;;; Source conditionalizations have been provided so that
;;; this code should work without alteration under:
;;;     Texas Instruments Common Lisp version 6.1
;;;     Macintosh Common LISP (Allegro/Coral) versions 1.3.2 and 2.0b1
;;;     Lucid Common LISP version 4.0.x, 4.1
;;;     Allegro Common LISP (Franz inc.) version 4.1
;;; If you are running under anything other than these you
;;; may be able to use one or other of the implementations
;;; (search for #+), but you may have to hack something new
;;; up for yourself.  The example of the implementations
;;; provided should make life simpler.
;;; It is worth noting that the performance improvement that
;;; can result from the pseudo-macro mechanism is usually
;;; substantial and can sometimes be tenfold.  Thus, if you are
;;; likely to be addressing any problems in which control of
;;; evaluation of arguments is necessary, particularly in problems
;;; that exhibit side-effects as a result of evaluating either
;;; the functions or the terminals in the function/terminal sets
;;; then is is very much worth your while to get this working
;;; on your own particular implementation.

;;; 3) The third component is the domain independent part of the
;;; program editing tool.

;;; 4) Is the definition of a set of rules for simplifying
;;; Boolean sexpressions.

;;; 5) Is the problem-specific code necessary to define and run
;;; the symbolic regression problem for 0.5x**2.

;;; 6) Is the code necessary to implement the Boolean 3-Majority-On
;;; problem

;;; 7) Is the code necessary to implement the Discrete Non-Hamstrung
;;; Squard Car problem.

;;; 8) Is a pair of test functions.  Test-GPP will execute all of the
;;; example test sexpressions shown in the book, printing out the tests
;;; as they are performed.  Time-Test-GPP runs test-GPP, sending the
;;; output from the tests to a log file, printing out the time taken
;;; at the end.

;;; 9) Commented out at the bottom you will find all of the forms
;;; provided in the book as examples of how to switch on the pseudo
;;; macro and fast-eval optimizations.  If you want to use these
;;; you need only compile this section out of the editor or remove
;;; the #| and |# marks and recompile the file.




;;; Notes:  When compiling you might get a warning about
;;; there being two definitions for the variable X
;;; and for the functions sort-population-by-fitness and
;;; define-function-set-for-MAJORITY-ON.
;;; This is intentional, so as to make the code below
;;; mirror the book as accurately as possible.  Although
;;; these warnings will not in any way prevent the GPP
;;; from working, you may choose to remove the second
;;; definition of (defvar X) and also whichever of the
;;; definitions of sort-population-by-fitness you do not
;;; want.  The default version of sort-population-by-fitness
;;; that you will get (i.e. the second) is the one which is
;;; likely to be most reproducible in behavior across
;;; platforms, but not necessarily the fastest.  The first
;;; definition of define-function-set-for-MAJORITY-ON is
;;; the more common usage for Boolean problems with each
;;; function represented in the function set just once.

;============================================================

;;; Copyright (c) John Koza, All rights reserved.
;;; U.S. Patent #4,935,877.  Other patents pending.

;============================================================

;;; Kernel

(defstruct individual
  program 
  (standardized-fitness 0)
  (adjusted-fitness 0)
  (normalized-fitness 0)
  (hits 0))

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
  "The method of selecting individuals in the population.
   Either :fitness-proportionate, :tournament or
   :fitness-proportionate-with-over-selection.")

(defvar *method-of-generation* :unbound
  "Can be any one of :grow, :full, :ramped-half-and-half")

(defvar *seed* :unbound
  "The seed for the Park-Miller congruential randomizer.")

(defvar *best-of-run-individual* :unbound
  "The best individual found during this run.")

(defvar *generation-of-best-of-run-individual* :unbound
  "The generation at which the best-of-run individual was found.")

(defun run-genetic-programming-system
           (problem-function
            seed
            maximum-generations
            size-of-population
            &rest seeded-programs)
;; Check validity of some arguments
  (assert (and (integerp maximum-generations)
               (not (minusp maximum-generations)))
          (maximum-generations)
          "Maximum-generations must be a non-negative ~
           integer, not ~S" maximum-generations)
  (assert (and (integerp size-of-population)
               (plusp size-of-population))
          (size-of-population)
          "Size-Of-Population must be a positive integer, ~
           not ~S" size-of-population)
  (assert (or (and (symbolp problem-function)
                   (fboundp problem-function))
              (functionp problem-function))
          (problem-function)
          "Problem-Function must be a function.")
  (assert (numberp seed) (seed)
          "The randomizer seed must be a number")
;; Set the global randomizer seed.
  (setf *seed* (coerce seed 'double-float))
;; Initialize best-of-run recording variables
  (setf *generation-of-best-of-run-individual* 0)
  (setf *best-of-run-individual* nil)
;; Get the six problem-specific functions needed to 
;; specify this problem as returned by a call to
;; problem-function
  (multiple-value-bind (function-set-creator
                        terminal-set-creator
                        fitness-cases-creator
                        fitness-function
                        parameter-definer
                        termination-predicate)
      (funcall problem-function)
;; Get the function set and its associated
;; argument map
    (multiple-value-bind (function-set argument-map)
        (funcall function-set-creator)
;; Set up the parameters using parameter-definer
      (funcall parameter-definer)
;; Print out parameters report
      (describe-parameters-for-run
        maximum-generations size-of-population)
;; Set up the terminal-set using terminal-set-creator
      (let ((terminal-set (funcall terminal-set-creator)))
;; Create the population
        (let ((population
                (create-population
                  size-of-population function-set argument-map
                  terminal-set seeded-programs)))
;; Define the fitness cases using the
;; fitness-cases-creator function
          (let ((fitness-cases (funcall fitness-cases-creator))
                ;; New-Programs is used in the breeding of the
                ;; new population.  Create it here to reduce
                ;; consing.
                (new-programs (make-array size-of-population)))
;; Now run the Genetic Programming Paradigm using 
;; the fitness-function and termination-predicate provided
            (execute-generations
              population new-programs fitness-cases
              maximum-generations fitness-function
              termination-predicate function-set
              argument-map terminal-set)
;; Finally print out a report
            (report-on-run)
;; Return the population and fitness cases
;; (for debugging) 
            (values population fitness-cases)))))))

(defun report-on-run ()
  "Prints out the best-of-run individual."
  (let ((*print-pretty* t))
    (format t "~5%The best-of-run individual program ~
               for this run was found on ~%generation ~D and had a ~
               standardized fitness measure ~
               of ~D and ~D hit~P.  ~%It was:~%~S"
            *generation-of-best-of-run-individual*
            (individual-standardized-fitness *best-of-run-individual*)
            (individual-hits *best-of-run-individual*)
            (individual-hits *best-of-run-individual*)
            (individual-program *best-of-run-individual*))))

(defun report-on-generation (generation-number population)
  "Prints out the best individual at the end of each generation"
  (let ((best-individual (aref population 0))
        (size-of-population (length population))
        (sum 0.0)
        (*print-pretty* t))
    ;; Add up all of the standardized fitnesses to get average
    (dotimes (index size-of-population)
      (incf sum (individual-standardized-fitness
                  (aref population index))))
    (format t "~2%Generation ~D:  Average standardized-fitness ~
               = ~S.  ~%~
               The best individual program of the population ~
               had a ~%standardized fitness measure of ~D ~
               and ~D hit~P. ~%It was: ~%~S"
            generation-number (/ sum (length population))
            (individual-standardized-fitness best-individual)
            (individual-hits best-individual)
            (individual-hits best-individual)
            (individual-program best-individual))))

(defun print-population (population)
  "Given a population, this prints it out (for debugging) "
  (let ((*print-pretty* t))
    (dotimes (index (length population))
      (let ((individual (aref population index)))
        (format t "~&~D   ~S    ~S"
                index
                (individual-standardized-fitness individual)
                (individual-program individual))))))

(defun describe-parameters-for-run
    (maximum-generations size-of-population)
  "Lists the parameter settings for this run."
  (format t "~2%Parameters used for this run.~
              ~%=============================")
  (format t "~%Maximum number of Generations:~50T~D"
          maximum-generations)
  (format t "~%Size of Population:~50T~D" size-of-population)
  (format t "~%Maximum depth of new individuals:~50T~D"
          *max-depth-for-new-individuals*)
  (format t "~%Maximum depth of new subtrees for mutants:~50T~D"
          *max-depth-for-new-subtrees-in-mutants*)
  (format t
     "~%Maximum depth of individuals after crossover:~50T~D"
     *max-depth-for-individuals-after-crossover*)
  (format t
     "~%Fitness-proportionate reproduction fraction:~50T~D"
     *fitness-proportionate-reproduction-fraction*)
  (format t "~%Crossover at any point fraction:~50T~D"
          *crossover-at-any-point-fraction*)
  (format t "~%Crossover at function points fraction:~50T~D"
          *crossover-at-function-point-fraction*)
  (format t "~%Number of fitness cases:~50T~D"
          *number-of-fitness-cases*)
  (format t "~%Selection method: ~50T~A" *method-of-selection*)
  (format t "~%Generation method: ~50T~A" *method-of-generation*)
  (format t "~%Randomizer seed: ~50T~D" *seed*))

(defvar *generation-0-uniquifier-table*
        (make-hash-table :test #'equal)
  "Used to guarantee that all generation 0 individuals
   are unique")

(defun create-population (size-of-population function-set
                          argument-map terminal-set
                          seeded-programs)
  "Creates the population.  This is an array of size
   size-of-population that is initialized to contain individual
   records.  The Program slot of each individual is initialized
   to a suitable random program except for the first N programs,
   where N = (length seeded-programs).  For these first N
   individuals the individual is initialized with the respective
   seeded program.  This is very useful in debugging."
  (let ((population (make-array size-of-population))
        (minimum-depth-of-trees 1)
        (attempts-at-this-individual 0)
        (full-cycle-p nil))
    (do ((individual-index 0))
        ((>= individual-index size-of-population))
      (when (zerop (mod individual-index
                        (max 1 (- *max-depth-for-new-individuals*
                               minimum-depth-of-trees))))
        (setf full-cycle-p (not full-cycle-p)))
      (let ((new-program
              (if (< individual-index (length seeded-programs))
                  ;; Pick a seeded individual
                  (nth individual-index seeded-programs)
                  ;; Create a new random program.
                  (create-individual-program
                    function-set argument-map terminal-set 
                    (ecase *method-of-generation*
                      ((:full :grow) *max-depth-for-new-individuals*)
                      (:ramped-half-and-half
                        (+ minimum-depth-of-trees
                           (mod individual-index
                                (- *max-depth-for-new-individuals*
                                   minimum-depth-of-trees)))))
                    t
                    (ecase *method-of-generation*
                      (:full t)
                      (:grow nil)
                      (:ramped-half-and-half
                        full-cycle-p))))))
        ;; Check if we have already created this program.
        ;; If not then store it and move on.
        ;; If we have then try again.
        (cond ((< individual-index (length seeded-programs))
               (setf (aref population individual-index)
                     (make-individual :program new-program))
               (incf individual-index))
              ((not (gethash new-program
                             *generation-0-uniquifier-table*))
               (setf (aref population individual-index)
                     (make-individual :program new-program))
               (setf (gethash new-program
                              *generation-0-uniquifier-table*)
                     t)
               (setf attempts-at-this-individual 0)
               (incf individual-index))
              ((> attempts-at-this-individual 20)
               ;; Then this depth has probably filled up, so
               ;; bump the depth counter.
               (incf minimum-depth-of-trees)
               ;; Bump the max depth too to keep in line with new minimum.
               (setf *max-depth-for-new-individuals*
                     (max *max-depth-for-new-individuals*
                          minimum-depth-of-trees)))
              (:otherwise (incf attempts-at-this-individual)))))
    ;; Flush out uniquifier table to that no pointers
    ;; are kept to generation 0 individuals.
    (clrhash *generation-0-uniquifier-table*)
    ;; Return the population that we've just created.
    population))

(defun choose-from-terminal-set (terminal-set)
  "Chooses a random terminal from the terminal set. 
   If the terminal chosen is the ephemeral
   :Floating-Point-Random-Constant,
   then a floating-point single precision random constant
   is created in the range -5.0->5.0.
   If :Integer-Random-Constant is chosen then an integer random 
   constant is generated in the range -10 to +10."
  (let ((choice (nth (random-integer (length terminal-set))
                     terminal-set)))
    (case choice
      (:floating-point-random-constant
        ;; pick a random number in the range -5.0 ---> +5.0.
        ;; Coerce it to be single precision floating-point.
        ;; Double precision is more expensive
        ;; A similar clause to this could be used to coerce it
        ;; to double prevision if you really need
        ;; double precision.
        ;; This is also the place to modify if you need a range
        ;; other than -5.0 ---> +5.0.
        (coerce (- (random-floating-point-number 10.0) 5.0)
                'single-float))
      (:integer-random-constant
        ;; pick a random integer in the range -10 ---> +10.
        (- (random-integer 21) 10))
      (otherwise choice))))

(defun create-individual-program
           (function-set argument-map terminal-set
            allowable-depth top-node-p full-p)
  "Creates a program recursively using the specified functions
   and terminals.  Argument map is used to determine how many
   arguments each function in the function set is supposed to
   have if it is selected.  Allowable depth is the remaining
   depth of the tree we can create, when we hit zero we will
   only select terminals.  Top-node-p is true only when we
   are being called as the top node in the tree.  This allows
   us to make sure that we always put a function at the top
   of the tree.  Full-p indicates whether this individual
   is to be maximally bushy or not."
  (cond ((<= allowable-depth 0)
         ;; We've reached maxdepth, so just pack a terminal.
         (choose-from-terminal-set terminal-set))
        ((or full-p top-node-p)
         ;; We are the top node or are a full tree,
         ;; so pick only a function.
         (let ((choice (random-integer (length function-set))))
           (let ((function (nth choice function-set))
                 (number-of-arguments
                   (nth choice argument-map)))
             (cons function
                   (create-arguments-for-function
                     number-of-arguments function-set
                     argument-map terminal-set
                     (- allowable-depth 1) full-p)))))
        (:otherwise
         ;; choose one from the bag of functions and terminals.
         (let ((choice (random-integer
                         (+ (length terminal-set)
                            (length function-set)))))
           (if (< choice (length function-set))
               ;; We chose a function, so pick it out and go
               ;; on creating the tree down from here.
               (let ((function (nth choice function-set))
                     (number-of-arguments
                       (nth choice argument-map)))
                 (cons function
                       (create-arguments-for-function
                         number-of-arguments function-set
                         argument-map terminal-set
                         (- allowable-depth 1) full-p)))
               ;; We chose an atom, so pick it out.
               (choose-from-terminal-set terminal-set))))))

(defun create-arguments-for-function
           (number-of-arguments function-set
            argument-map terminal-set allowable-depth
            full-p)
  "Creates the argument list for a node in the tree.  
   Number-Of-Arguments is the number of arguments still
   remaining to be created.  Each argument is created
   in the normal way using Create-Individual-Program."
  (if (= number-of-arguments 0)
      nil
      (cons (create-individual-program
              function-set argument-map terminal-set
              allowable-depth nil full-p)
            (create-arguments-for-function
              (- number-of-arguments 1) function-set
              argument-map terminal-set
              allowable-depth full-p))))

(defun execute-generations
    (population new-programs fitness-cases maximum-generations
     fitness-function termination-predicate function-set
     argument-map terminal-set)
  "Loops until the user's termination predicate says to stop."
  (do ((current-generation 0 (+ 1 current-generation)))
      ;; loop incrementing current generation until
      ;; termination-predicate succeeds.
      ((let ((best-of-generation (aref population 0)))
         (funcall
           termination-predicate current-generation
           maximum-generations
           (individual-standardized-fitness best-of-generation)
           (individual-hits best-of-generation))))
    (when (> current-generation 0)
      ;; Breed the new population to use on this generation
      ;; (except gen 0, of course).
      (breed-new-population population new-programs function-set
                            argument-map terminal-set))
    ;; Clean out the fitness measures.
    (zeroize-fitness-measures-of-population population)
    ;; Measure the fitness of each individual.  Fitness values
    ;; are stored in the individuals themselves.
    (evaluate-fitness-of-population
      population fitness-cases fitness-function)
    ;; Normalize fitness in preparation for crossover, etc.
    (normalize-fitness-of-population population)
    ;; Sort the population so that the roulette wheel is easy.
    (sort-population-by-fitness population)
    ;; Keep track of best-of-run individual
    (let ((best-of-generation (aref population 0)))
      (when (or (not *best-of-run-individual*)
                (> (individual-standardized-fitness *best-of-run-individual*)
                   (individual-standardized-fitness best-of-generation)))
        (setf *best-of-run-individual* (copy-individual best-of-generation))
        (setf *generation-of-best-of-run-individual* current-generation)))
    ;; Print out the results for this generation.
    (report-on-generation current-generation population)))

(defun zeroize-fitness-measures-of-population (population)
  "Clean out the statistics in each individual in the
   population.  This is not strictly necessary, but it helps to
   avoid confusion that might be caused if, for some reason, we
   land in the debugger and there are fitness values associated 
   with the individual records that actually matched the program 
   that used to occupy this individual record."
  (dotimes (individual-index (length population))
    (let ((individual (aref population individual-index)))
      (setf (individual-standardized-fitness individual) 0.0)
      (setf (individual-adjusted-fitness individual) 0.0)
      (setf (individual-normalized-fitness individual) 0.0)
      (setf (individual-hits individual) 0))))

(defun evaluate-fitness-of-population (population fitness-cases
                                       fitness-function)
  "Loops over the individuals in the population evaluating and
   recording the fitness and hits."
  (dotimes (individual-index (length population))
    (let ((individual (aref population individual-index)))
      (multiple-value-bind (standardized-fitness hits)
          (funcall fitness-function
                   (individual-program individual)
                   fitness-cases)
        ;; Record fitness and hits for this individual.
        (setf (individual-standardized-fitness individual)
              standardized-fitness)
        (setf (individual-hits individual) hits)))))

(defun normalize-fitness-of-population (population)
  "Computes the normalized and adjusted fitness of each
   individual in the population."
  (let ((sum-of-adjusted-fitnesses 0.0))
    (dotimes (individual-index (length population))
      (let ((individual (aref population individual-index)))
        ;; Set the adjusted fitness.
        (setf (individual-adjusted-fitness individual)
              (/ 1.0 (+ 1.0 (individual-standardized-fitness
                              individual))))
        ;; Add up the adjusted fitnesses so that we can
        ;; normalize them.
        (incf sum-of-adjusted-fitnesses
              (individual-adjusted-fitness individual))))
    ;; Loop through population normalizing the adjusted fitness.
    (dotimes (individual-index (length population))
      (let ((individual (aref population individual-index)))
        (setf (individual-normalized-fitness individual)
              (/ (individual-adjusted-fitness individual)
                 sum-of-adjusted-fitnesses))))))

(defun sort-population-by-fitness (population)
  "Sorts the population according to normalized fitness. 
   The population array is destructively modified."
  (sort population #'> :key #'individual-normalized-fitness))

(defun sort-population-by-fitness
    (population &optional (low 0) (high (length population)))
  "Uses a trivial quicksort to sort the population destructively
   into descending order of normalized fitness." 
 (unless (>= (+ low 1) high)
    (let ((pivot (individual-normalized-fitness (aref population low)))
          (index1 (+ low 1))
          (index2 (- high 1)))
      (loop (do () ((or (>= index1 high)
                        (<= (individual-normalized-fitness
                             (aref population index1)) pivot)))
              (incf index1))
            (do () ((or (>= low index2)
                        (>= (individual-normalized-fitness
                             (aref population index2)) pivot)))
              (decf index2))
            (when (>= index1 index2) (return nil))
            (rotatef (aref population index1) (aref population index2))
            (decf index2))
      (rotatef (aref population low) (aref population (- index1 1)))
      (sort-population-by-fitness population low index1)
      (sort-population-by-fitness population index1 high)))
  population)

(defun breed-new-population
      (population new-programs function-set
       argument-map terminal-set)
  "Controls the actual breeding of the new population.  
   Loops through the population executing each operation
   (e.g., crossover, fitness-proportionate reproduction,
   mutation) until it has reached the specified fraction.  
   The new programs that are created are stashed in new-programs
   until we have exhausted the population, then we copy the new
   individuals into the old ones, thus avoiding consing a new
   bunch of individuals."
  (let ((population-size (length population)))
    (do ((index 0)
         (fraction 0 (/ index population-size)))
        ((>= index population-size))
      (let ((individual-1
              (find-individual population)))
        (cond ((and (< index (- population-size 1))
                    (< fraction
                       (+ *crossover-at-function-point-fraction*
                          *crossover-at-any-point-fraction*)))
               (multiple-value-bind (new-male new-female)
                 (funcall
                  (if (< fraction
                         *crossover-at-function-point-fraction*)
                      'crossover-at-function-points
                      'crossover-at-any-points)
                  individual-1
                  (find-individual population))
                 (setf (aref new-programs index) new-male)
                 (setf (aref new-programs (+ 1 index))
                       new-female))
               (incf index 2))
              ((< fraction
                (+ *fitness-proportionate-reproduction-fraction*
                   *crossover-at-function-point-fraction*
                   *crossover-at-any-point-fraction*))
               (setf (aref new-programs index) individual-1)
               (incf index 1))
              (:otherwise
	       (setf (aref new-programs index)
		     (mutate individual-1 function-set
			     argument-map terminal-set))
	       (incf index 1)))))
    (dotimes (index population-size)
      (setf (individual-program (aref population index))
            (aref new-programs index)))))

(defun find-individual (population)
  "Finds an individual in the population according to the
   defined selection method."
  (ecase *method-of-selection*
    (:tournament (find-individual-using-tournament-selection
                   population))
    (:fitness-proportionate-with-over-selection
      (find-fitness-proportionate-individual
        (random-floating-point-number-with-over-selection
          population)
        population))
    (:fitness-proportionate
      (find-fitness-proportionate-individual
        (random-floating-point-number 1.0) population))))

(defun random-floating-point-number-with-over-selection (population)
  "Picks a random number between 0.0 and 1.0 biased using the
   over-selection method."
  (let ((pop-size (length population)))
    (when (< pop-size 1000)
      (error "A population size of ~D is too small ~
              for over-selection." pop-size))
    (let ((boundary (/ 320.0 pop-size)))
      ;; The boundary between the over and under selected parts.
      (if (< (random-floating-point-number 1.0) 0.8)
          ;; 80% are in the over-selected part
          (random-floating-point-number boundary)
          (+ boundary
	     (random-floating-point-number (- 1.0 boundary)))))))

(defun find-individual-using-tournament-selection (population)
  "Picks two individuals from the population at random and
   returns the better one."
  (let ((individual-a
          (aref population 
                (random-integer (length population))))
        (individual-b
          (aref population 
                (random-integer (length population)))))
    (if (< (individual-standardized-fitness individual-a)
           (individual-standardized-fitness individual-b))
        (individual-program individual-a)
        (individual-program individual-b))))

(defun find-fitness-proportionate-individual
    (after-this-fitness population)
  "Finds an individual in the specified population whose
   normalized fitness is greater than the specified value.
   All we need to do is count along the population from the
   beginning adding up the fitness until we get past the
   specified point."
  (let ((sum-of-fitness 0.0)
        (population-size (length population)))
    (let ((index-of-selected-individual
            (do ((index 0 (+ index 1)))
                ;; Exit condition
                ((or (>= index population-size)
                     (>= sum-of-fitness after-this-fitness))
                 (if (>= index population-size)
                     (- (length population) 1)
                     (- index 1)))
              ;; Body.  Sum up the fitness values.
              (incf sum-of-fitness
                    (individual-normalized-fitness
                      (aref population index))))))
      (individual-program
        (aref population index-of-selected-individual)))))

(defun crossover-at-any-points (male female)
  "Performs crossover on the programs at any point
   in the trees."
  ;; Pick points in the respective trees 
  ;; on which to perform the crossover.
  (let ((male-point
          (random-integer (count-crossover-points male)))
        (female-point
          (random-integer (count-crossover-points female))))
    ;; First, copy the trees because we destructively modify the
    ;; new individuals to do the crossover.  Reselection is
    ;; allowed in the original population.  Not copying would
    ;; cause the individuals in the old population to
    ;; be modified.
    (let ((new-male   (list (copy-tree male)))
          (new-female (list (copy-tree female))))
      ;; Get the pointers to the subtrees indexed by male-point
      ;; and female-point
      (multiple-value-bind (male-subtree-pointer male-fragment)
          (get-subtree (first new-male) new-male male-point)
        (multiple-value-bind
          (female-subtree-pointer female-fragment)
            (get-subtree
              (first new-female) new-female female-point)
          ;; Modify the new individuals by smashing in the
          ;; (copied) subtree from the old individual.
          (setf (first   male-subtree-pointer) female-fragment)
          (setf (first female-subtree-pointer) male-fragment)))
      ;; Make sure that the new individuals aren't too big.
      (validate-crossover male new-male female new-female))))

(defun count-crossover-points (program)
  "Counts the number of points in the tree (program).  
   This includes functions as well as terminals."
  (if (consp program)
      (+ 1 (reduce #'+ (mapcar #'count-crossover-points
                               (rest program))))
      1))

(defun max-depth-of-tree (tree)
  "Returns the depth of the deepest branch of the
   tree (program)."
  (if (consp tree)
      (+ 1 (if (rest tree)
               (apply #'max
                      (mapcar #'max-depth-of-tree (rest tree)))
               0))
      1))

(defun get-subtree (tree pointer-to-tree index)
  "Given a tree or subtree, a pointer to that tree/subtree and
   an index return the component subtree that is numbered by
   Index.  We number left to right, depth first."
  (if (= index 0)
      (values pointer-to-tree (copy-tree tree) index)
      (if (consp tree)
          (do* ((tail (rest tree) (rest tail))
                (argument (first tail) (first tail)))
               ((not tail) (values nil nil index))
            (multiple-value-bind
                (new-pointer new-tree new-index)
                (get-subtree argument tail (- index 1))
              (if (= new-index 0)
                  (return
                    (values new-pointer new-tree new-index))
                  (setf index new-index))))
          (values nil nil index))))

(defun validate-crossover (male new-male female new-female)
  "Given the old and new males and females from a crossover
   operation check to see whether we have exceeded the maximum
   allowed depth.  If either of the new individuals has exceeded
   the maxdepth then the old individual is used."
  (let ((male-depth   (max-depth-of-tree (first new-male)))
        (female-depth (max-depth-of-tree (first new-female))))
    (values
      (if (or (= 1 male-depth)
              (> male-depth
                 *max-depth-for-individuals-after-crossover*))
          male
          (first new-male))
      (if (or (= 1 female-depth)
              (> female-depth
                 *max-depth-for-individuals-after-crossover*))
          female
          (first new-female)))))

(defun crossover-at-function-points (male female)
  "Performs crossover on the two programs at a function
   (internal) point in the trees."
  ;; Pick the function (internal) points in the respective trees 
  ;; on which to perform the crossover.
  (let ((male-point
          (random-integer (count-function-points male)))
        (female-point
          (random-integer (count-function-points female))))
    ;; Copy the trees because we destructively modify the new
    ;; individuals to do the crossover and Reselection is
    ;; allowed in the original population. Not copying would
    ;; cause the individuals in the old population to
    ;; be modified.
    (let ((new-male   (list (copy-tree male)))
          (new-female (list (copy-tree female))))
      ;; Get the pointers to the subtrees indexed by male-point
      ;; and female-point
      (multiple-value-bind (male-subtree-pointer male-fragment)
          (get-function-subtree
            (first new-male) new-male male-point)
        (multiple-value-bind
          (female-subtree-pointer female-fragment)
            (get-function-subtree
              (first new-female) new-female female-point)
          ;; Modify the new individuals by smashing in
          ;; the (copied) subtree from the old individual.
          (setf (first   male-subtree-pointer) female-fragment)
          (setf (first female-subtree-pointer) male-fragment)))
      ;; Make sure that the new individuals aren't too big.
      (validate-crossover male new-male female new-female))))

(defun count-function-points (program)
  "Counts the number of function (internal) points
   in the program."
  (if (consp program)
      (+ 1 (reduce #'+ (mapcar #'count-function-points
                               (rest program))))
      0))

(defun get-function-subtree (tree pointer-to-tree index)
  "Given a tree or subtree, a pointer to that tree/subtree and
   an index return the component subtree that is labeled with
   an internal point that is numbered by Index.  We number left
   to right, depth first."
  (if (= index 0)
      (values pointer-to-tree (copy-tree tree) index)
      (if (consp tree)
          (do* ((tail (rest tree) (rest tail))
                (argument (first tail) (first tail)))
               ((not tail) (values nil nil index))
            (multiple-value-bind
              (new-pointer new-tree new-index)
                (if (consp argument)
                    (get-function-subtree
                      argument tail (- index 1))
                    (values nil nil index))
              (if (= new-index 0)
                  (return
                    (values new-pointer new-tree new-index))
                  (setf index new-index))))
          (values nil nil index))))

(defun mutate (program function-set argument-map terminal-set)
  "Mutates the argument program by picking a random point in
   the tree and substituting in a brand new subtree created in
   the same way that we create the initial random population."
  ;; Pick the mutation point.
  (let ((mutation-point
          (random-integer (count-crossover-points program)))
        ;; Create a brand new subtree.
        (new-subtree
          (create-individual-program
            function-set argument-map terminal-set
            *max-depth-for-new-subtrees-in-mutants* t nil)))
    (let ((new-program (list (copy-tree program))))
      (multiple-value-bind (subtree-pointer fragment)
          ;; Get the pointer to the mutation point.
          (get-subtree (first new-program)
                       new-program mutation-point)
        ;; Not interested in what we're snipping out.
        (declare (ignore fragment))
        ;; Smash in the new subtree.
        (setf (first subtree-pointer) new-subtree))
      (values (first new-program) new-subtree))))

(defun park-miller-randomizer ()
  "The Park-Miller multiplicative congruential randomizer
   (CACM, October 88, Page 1195).  Creates pseudo random floating
   point numbers in the range 0.0 < x <= 1.0.  The seed value
   for this randomizer is called *seed*, so you should
   record/set this if you want to make your runs reproducible."
  #+Lucid (unless (typep *seed* 'integer) (setq *seed* (round *seed*)))
  (assert (not (zerop *seed*)) () "*seed* cannot be zero.")
  (let ((multiplier #+Lucid 16807 #-Lucid 16807.0d0);16807 is (expt 7 5)
        (modulus #+Lucid 2147483647 #-Lucid 2147483647.0d0))
             ;2147483647 is (- (expt 2 31) 1)
    (let ((temp (* multiplier *seed*)))
      (setf *seed* (mod temp modulus))
      ;;Produces floating-point number in the range
      ;;  0.0 < x <= 1.0
      (#+lucid float #-lucid progn (/ *seed* modulus)))))

(defun random-floating-point-number (n)
  "Returns a pseudo random floating-point number
                in range 0.0 <= number < n"
  (let ((random-number (park-miller-randomizer)))
    ;; We subtract the randomly generated number from 1.0
    ;; before scaling so that we end up in the range
    ;; 0.0 <= x < 1.0, not 0.0 < x <= 1.0
    (* n (- 1.0d0 random-number))))

(defun random-integer (n)
  "Returns a pseudo-random integer in the range 0 ---> n-1."
  (let ((random-number (random-floating-point-number 1.0)))
    (floor (* n random-number))))

;=========================================================================

;;; Streamlined EVAL

(defmacro fast-eval-fun ()
  "A code body that does fast evaluation of a
   functional expression."
  '(ecase (length expr)
     (1 (funcall fef))
     (2 (funcall fef
                 (fast-eval
                  (second expr))))
     (3 (funcall fef
                 (fast-eval (second expr))
                 (fast-eval
                  (third  expr))))
     (4 (funcall fef
                 (fast-eval (second expr))
                 (fast-eval (third  expr))
                 (fast-eval
                  (fourth expr))))))

#+TI
(defun fast-eval (expr)
  "A fast evaluator that can be used with the
   Genetic Programming Paradigm for the TI Explorer."
  (cond ((consp expr)
         (let ((function (first expr)))
              (if (eq 'quote function)
                  (second expr)
                  (let ((fef (symbol-function function)))
                       (cond ((and (consp fef)
                                   (eq'pseudo-macro (first fef)))
                              (apply (second fef) (rest expr)))
                             (t (fast-eval-fun)))))))
        ((symbolp expr) (symbol-value expr))
        (t expr)))

#+:CCL
(defvar *pseudo-macro-tag* (compile nil '(lambda () nil)))

#+:CCL
(defun fast-eval (expr)
  "A fast evaluator that can be used with the
   Genetic Programming Paradigm for Macintosh Common Lisp."
  (cond ((consp expr)
         (let ((function (first expr)))
              (if (eq 'quote function)
                  (second expr)
                  (let ((fef (symbol-function function)))
                       (cond ((eq fef *pseudo-macro-tag*)
                              (apply (symbol-value function)
                                     (rest expr)))
                             (t (fast-eval-fun)))))))
        ((symbolp expr) (symbol-value expr))
        (t expr)))

#+EXCL
(defun fast-eval (expr)
  "A fast evaluator that can be used with the
   Genetic Programming Paradigm for Franz Lisp."
  (cond ((consp expr)
         (let ((function (first expr)))
              (if (eq 'quote function)
                  (second expr)
                  (let ((fef (symbol-function function)))
                       (cond ((compiled-function-p fef)
                              (fast-eval-fun))
                             ;; Then ASSUME we are a pseudo
                             ;; macro and are bound.
                             (t (apply (symbol-value function)
                                       (rest expr))))))))
        ((symbolp expr) (symbol-value expr))
        (t expr)))

#+Lucid
(defconstant *pseudo-macro-flag-position* 20)

#+Lucid
(defun fast-eval (expr)
  "A fast evaluator that can be used with the
   Genetic Programming Paradigm for Lucid Lisp."
  (declare (optimize (speed 3) (safety 0) (compilation-speed 0)))
  (cond ((consp expr)
         (let ((function (first expr)))
              (if (eq 'quote function)
                  (second expr)
                  (let ((fef (symbol-function function)))
		    (if (logbitp (the fixnum *pseudo-macro-flag-position*)
				 (the fixnum
				      (system:procedure-ref
					fef system:procedure-flags)))
			;; Then ASSUME we are a pseudo
			;; macro and are bound.
			(apply (symbol-value function) (rest expr))
			(fast-eval-fun))))))
        ((symbolp expr) (symbol-value expr))
        (t expr)))

(defun install-pseudo-macro (name implementation)
  "Install a pseudo-macro called Name, which is implemented by the function
   Implementation."
  #+(or EXCL Lucid :CCL)
  (setf (symbol-value name) implementation)
  (setf (symbol-function name)
        #+:CCL *pseudo-macro-tag*
	#+Lucid implementation
        #-(or Lucid :CCL)
        (list #+TI 'pseudo-macro
              #+(or EXCL :CCL) 'lambda
              #-(or TI EXCL :CCL)
                (error "A conditionalization for your lisp ~
                        must be added to install-pseudo-macro")
              implementation))
  #+Lucid
  (setf (ldb (byte 1 *pseudo-macro-flag-position*)
	     (system:procedure-ref implementation system:procedure-flags))
	1)
  (format t "~&;;; Installed ~S as the implementation of ~S"
          implementation name))

;;; Detect those implementations that know about fast-eval
(eval-when (compile load eval)
  #+(or Lucid EXCL TI :CCL)
  (pushnew :Fast-Eval *features*)
  nil)

#-:Fast-Eval
(warn "No implementation-specific version of fast-eval ~
       has been written.  Please write your own using ~
       the examples provided.")

(defun ifltz-implementation (then-clause else-clause)
  "An example implementation of a pseudo-macro.  Note that the
   arguments are evaluated using fast-eval explicitly.
   This implements ifltz, the if x < 0 then do Then-clause
   else do the Else-clause."
  (declare (special x))
  (if (< x 0) (fast-eval then-clause) (fast-eval else-clause)))

;;; Registers ifltz-implementation as the implementation
;;;  of ifltz
#+:Fast-eval
(install-pseudo-macro 'ifltz #'ifltz-implementation)

(defun my-if-implementation (condition then-clause else-clause)
  "Implements MY-IF, which is a pseudo-macro just like IF."
  (if (fast-eval condition)
      (fast-eval then-clause)
      (fast-eval else-clause)))

;;; Registers my-if-implementation as the implementation of
;;;  my-if
#+:Fast-eval
(install-pseudo-macro 'my-if #'my-if-implementation)

(defun sand (a b)
  "Strict AND"
  (and a b))

(defun sor (a b)
  "Strict OR"
  (or a b))

;=========================================================================

;;; Editor for simplifying sexpressions

(defun edit-top-level-sexpression (sexpression rule-base)
  "Applies the rules in RULE-BASE to edit SEXPRESSION into
   a simpler form."
  (let ((location (list sexpression)))
    (edit-sexpression rule-base location sexpression)
    location))

(defun edit-sexpression (rule-base location sexpression)
  "Given a rule base (list of rules), an sexpression and the
   location of that sexpression in the containing expression,
   applies the rules to the sexpression and its arguments 
   recursively.  The rules are reapplied until a quiescent state 
   is achieved."
  ;; Apply the edit rules to each of the arguments.
  ;; If something changes, try again.
  (when (consp sexpression)
    (do* ((args (rest sexpression) (rest args))
          (arg (first args) (first args))
          (arg-location (rest sexpression) (rest arg-location))
          (changed-p
            (edit-sexpression rule-base arg-location arg)
            (edit-sexpression rule-base arg-location arg)))
         ((not args)
          (when changed-p
            (edit-sexpression rule-base location sexpression)))
      nil))
  ;; Apply the edit rules to this expression.  Say that 
  ;; something has changed if any rule fires.
  (let ((changed-p nil))
    (dolist (clause rule-base)
      (let ((condition (second clause))
            (action (third clause)))
        (let ((applicable-p (funcall condition sexpression)))
          (when applicable-p
            (funcall action location sexpression)
            (setf changed-p t)))))
    changed-p))

(defun constant-expression-p (sexpression)
  "Is true of an sexpression if it evaluates to a constant.
   Note that this can be a problem domain specific problem."
  (if (consp sexpression)
      (do* ((args (rest sexpression) (rest args))
            (arg (first args) (first args)))
           ((not args) t)
        (unless (constant-expression-p arg)
          (return nil)))
      ;;; Assumes that variable quantities are always symbols
      ;;; and assumes that any symbol that is not self-
      ;;; evaluating is not constant (this will fail for pi)
      ;;; so to solve more general problems some extra
      ;;; convention would be required.
      (or (not (symbolp sexpression))
          (keywordp sexpression)
          (and (boundp sexpression)
               (eq sexpression (symbol-value sexpression))))))

(defmacro def-edit-rule  (rule-name rule-base (sexpression-name)
                          &key condition action)
  "Declares an edit rule called RULE-NAME in the RULE-BASE.  
   SEXPRESSION-NAME is the local name to be given to the 
   sexpression on which this rule is being invokes.  The
   CONDITION clause is evaluated, and if it is true, the
   ACTION clause is evaluated.  The action clause should 
   make calls to REPLACE-SEXPRESSION to perform an edit."
  (assert (and condition action) ()
    "Both a condition and an action must be supplied.")
  `(setf ,rule-base
         (cons (list ',rule-name
                     #'(lambda (,sexpression-name) ,condition)
                     #'(lambda (location ,sexpression-name)
                         ,sexpression-name ,action))
               (remove (assoc ',rule-name ,rule-base :test #'eq)
                       ,rule-base))))

(defmacro replace-sexpression (new-sexpression)
  "The form to use in an edit rule that registers an edit.
   For example, if the sexpression being edited is to be 
   replaced with the first argument to the function of the
  sexpression then we would say:  (replace-sexpression (second
  the-sexpression)), where the-sexpression is the name of the
  sexpression supplied as an argument to def-edit-rule.  This
  example would be useful if the function in question was an
 identity function.  Thus:
  (def-edit-rule remove-identity-functions *my-rule-base*
                 (the-sexpression)
    :condition (and (consp the-sexpression)
                    (eq (first the-sexpression) 'identity))
    :action (replace-sexpression (second the-sexpression)))"
  `(setf (first location) ,new-sexpression))

;=========================================================================

;;; Robocode Problem

(defvar x energy heading x y max-x max-y enemy-bearing enemy-distance enemy-velocity enemy-heading enemy-energy zero)

(defun define-terminal-set-for-ROBOCODE ()
  (values '(x:constant-floating-point-random energy heading x y max-x max-y enemy-bearing enemy-distance enemy-velocity enemy-heading enemy-energy zero))
)

(defun define-function-set-for-ROBOCODE ()
  (values '(+ - * % abs neg sin cos arcsin arccos gt pos fire)
          '(2 2 2 2 2 2 2 2 2 2 2 2 1))
)

(defun % (numerator denominator)
  "The Protected Division Function"
  (values (if (= 0 denominator) 1 (/ numerator denominator)))
)

(defstruct ROBOCODE-fitness-case
    independent-variable
    target
)

(defun define-fitness-cases-for-ROBOCODE ()	;01
  (let (fitness-cases x this-fitness-case)	;02
    (setf fitness-cases (make-array *number-of-fitness-cases*))	;03
    (format t "~%Fitness cases")	;04
    (dotimes (index *number-of-fitness-cases*)	;05
      (setf x (/ index *number-of-fitness-cases*))	;06
      (setf this-fitness-case (make-ROBOCODE-fitness-case))	;07
      (setf (aref fitness-cases index) this-fitness-case)	;08
      (setf (ROBOCODE-fitness-case-independent-variable	;09
             this-fitness-case)	;10
              x)	;11
      (setf (ROBOCODE-fitness-case-target	;12
             this-fitness-case)	;13
            () )	;14 TODO
      (format t "~% ~D      ~D      ~D"	;15
            index	;16
            (float x)	;17
            (ROBOCODE-fitness-case-target this-fitness-case))	;18
    )	;19
    (values fitness-cases)	;20
  )	;21
)	;22

(defun ROBOCODE-wrapper (result-from-program)
  (values result-from-program)
)

(defun evaluate-standardized-fitness-for-ROBOCODE		;01
           (program fitness-cases)				;02
  (let (raw-fitness hits standardized-fitness x target-value	;03
        difference value-from-program this-fitness-case)	;04
    (setf raw-fitness 0.0)					;05
    (setf hits 0)						;06
    (dotimes (index *number-of-fitness-cases*)	;07
      (setf this-fitness-case (aref fitness-cases index))	;08
      (setf x	;09
            (ROBOCODE-fitness-case-independent-variable	;10
                   this-fitness-case))	;11
      (setf target-value	;12
             (ROBOCODE-fitness-case-target	;13
                   this-fitness-case))	;14
       (setf value-from-program	;15
             (ROBOCODE-wrapper (eval program)))	;16
       (setf difference (abs (- target-value	;17
                                     value-from-program)))	;18
      (incf raw-fitness difference)	;19
      (when (< difference 0.01) (incf hits)))	;20
    (setf standardized-fitness raw-fitness)	;21
    (values standardized-fitness hits)	;22
  )	;23
)	;24

(defun define-parameters-for-ROBOCODE ()
  (setf *number-of-fitness-cases* 10)
  (setf *max-depth-for-new-individuals* 6)
  (setf *max-depth-for-individuals-after-crossover* 17)
  (setf *fitness-proportionate-reproduction-fraction* 0.1)
  (setf *crossover-at-any-point-fraction* 0.2)
  (setf *crossover-at-function-point-fraction* 0.2)
  (setf *max-depth-for-new-subtrees-in-mutants* 4)
  (setf *method-of-selection* :fitness-proportionate)
  (setf *method-of-generation* :ramped-half-and-half)
  (values)
)

(defun define-termination-criterion-for-ROBOCODE	;01
       (current-generation	;02
        maximum-generations	;03
        best-standardized-fitness	;04
        best-hits)	;05
  (declare (ignore best-standardized-fitness))	;06
  (values	;07
    (or (>= current-generation maximum-generations)	;08
         (>= best-hits *number-of-fitness-cases*))	;09
  )	;10
)	;11

(defun ROBOCODE ()
  (values 'define-function-set-for-ROBOCODE
          'define-terminal-set-for-ROBOCODE
          'define-fitness-cases-for-ROBOCODE
          'evaluate-standardized-fitness-for-ROBOCODE
          'define-parameters-for-ROBOCODE
          'define-termination-criterion-for-ROBOCODE
  )
)

;=========================================================================

;;; Regression Problem for 0.5x**2

(defvar x)

(defun define-terminal-set-for-REGRESSION ()
  (values '(x :floating-point-random-constant))
)

(defun define-function-set-for-REGRESSION ()
  (values '(+ - * %)
          '(2 2 2 2))
)

(defun % (numerator denominator)
  "The Protected Division Function"
  (values (if (= 0 denominator) 1 (/ numerator denominator)))
)

(defstruct REGRESSION-fitness-case
    independent-variable
    target
)

(defun define-fitness-cases-for-REGRESSION ()	;01
  (let (fitness-cases x this-fitness-case)	;02
    (setf fitness-cases (make-array *number-of-fitness-cases*))	;03
    (format t "~%Fitness cases")	;04
    (dotimes (index *number-of-fitness-cases*)	;05
      (setf x (/ index *number-of-fitness-cases*))	;06
      (setf this-fitness-case (make-REGRESSION-fitness-case))	;07
      (setf (aref fitness-cases index) this-fitness-case)	;08
      (setf (REGRESSION-fitness-case-independent-variable	;09
             this-fitness-case)	;10
              x)	;11
      (setf (REGRESSION-fitness-case-target	;12
             this-fitness-case)	;13
            (* 0.5 x x))	;14
      (format t "~% ~D      ~D      ~D"	;15
            index	;16
            (float x)	;17
            (REGRESSION-fitness-case-target this-fitness-case))	;18
    )	;19
    (values fitness-cases)	;20
  )	;21
)	;22

(defun REGRESSION-wrapper (result-from-program)
  (values result-from-program)
)

(defun evaluate-standardized-fitness-for-REGRESSION		;01
           	(program fitness-cases)				;02
  (let (raw-fitness hits standardized-fitness x target-value	;03
        difference value-from-program this-fitness-case)	;04
    (setf raw-fitness 0.0)					;05
    (setf hits 0)						;06
    (dotimes (index *number-of-fitness-cases*)			;07
      (setf this-fitness-case (aref fitness-cases index))	;08
      (setf x							;09
            (REGRESSION-fitness-case-independent-variable	;10
                   this-fitness-case))				;11
      (setf target-value					;12
             (REGRESSION-fitness-case-target			;13
                   this-fitness-case))				;14
       (setf value-from-program					;15
             (REGRESSION-wrapper (eval program)))		;16
       (setf difference (abs (- target-value			;17
                                     value-from-program)))	;18
      (incf raw-fitness difference)				;19
      (when (< difference 0.01) (incf hits)))			;20
    (setf standardized-fitness raw-fitness)			;21
    (values standardized-fitness hits)				;22
  )								;23
)								;24

(defun define-parameters-for-REGRESSION ()
  (setf *number-of-fitness-cases* 10)
  (setf *max-depth-for-new-individuals* 6)
  (setf *max-depth-for-individuals-after-crossover* 17)
  (setf *fitness-proportionate-reproduction-fraction* 0.1)
  (setf *crossover-at-any-point-fraction* 0.2)
  (setf *crossover-at-function-point-fraction* 0.2)
  (setf *max-depth-for-new-subtrees-in-mutants* 4)
  (setf *method-of-selection* :fitness-proportionate)
  (setf *method-of-generation* :ramped-half-and-half)
  (values)
)

(defun define-termination-criterion-for-REGRESSION	;01
       (current-generation				;02
        maximum-generations				;03
        best-standardized-fitness			;04
        best-hits)					;05
  (declare (ignore best-standardized-fitness))		;06
  (values						;07
    (or (>= current-generation maximum-generations)	;08
         (>= best-hits *number-of-fitness-cases*))	;09
  )							;10
)							;11

(defun REGRESSION ()
  (values 'define-function-set-for-REGRESSION
          'define-terminal-set-for-REGRESSION
          'define-fitness-cases-for-REGRESSION
          'evaluate-standardized-fitness-for-REGRESSION
          'define-parameters-for-REGRESSION
          'define-termination-criterion-for-REGRESSION
  )
)

;=========================================================================

;;; Robocode problem

(defvar x)
(defvar y)

(defun define-parameters-for-ROBOCODE ()
  (setf *number-of-fitness-cases* 8)
  (setf *max-depth-for-new-individuals* 6)
  (setf *max-depth-for-new-subtrees-in-mutants* 4)
  (setf *max-depth-for-individuals-after-crossover* 17)
  (setf *fitness-proportionate-reproduction-fraction* 0.1)
  (setf *crossover-at-any-point-fraction* 0.2)
  (setf *crossover-at-function-point-fraction* 0.7)
  (setf *method-of-selection* :fitness-proportionate)
  (setf *method-of-generation* :ramped-half-and-half)
  (values)
)

(defun define-termina-set-for-ROBOCODE ()
  (value '((GetEnergy)))
)


;=========================================================================

;;; Test harness.

(defun test-gpp (&optional (report-stream *standard-output*))
  (let ((tests
         '(           
	
	(run-genetic-programming-system 'REGRESSION 1.0 1 50)

          )
        ))
    (dolist (form tests)
      (eval form)
      (format report-stream "~&Finished test ~S" form))))

(apply (test-gpp *standard-output*))




