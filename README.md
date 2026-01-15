# OWLAPI-Lite
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/dwyl/esta/issues)
[![Maven Central](https://img.shields.io/maven-central/v/net.sourceforge.simpleowlapi/simpleowlapi-lib.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22net.sourceforge.simpleowlapi%22%20AND%20a:%22simpleowlapi-lib%22)

OWLAPI-Lite is a light-weight wrapper for the [OWLAPI](https://github.com/owlcs/owlapi) that enables faster development of [OWL](https://www.w3.org/TR/owl2-overview/) ontologies with more concise code using the power of the [Manchester OWL Syntax](https://www.w3.org/TR/owl2-manchester-syntax/) and related parsers. The wrapper does not make use of all the features of the more powerful OWLAPI. Rather, it is designed specifically for rapid and concise ontology construction in workflows involving semantic data engineering. It can also be used in contexts such as educational and demonstration settings in courses, workshops and tutorials. In particular, the library can also be used in [Jupyter notebooks](https://jupyter.org/) using a Java kernel such as the [IJava kernel](https://github.com/SpencerPark/IJava).

### Usage in Jupyter notebooks

##### Requirements

1. [Jupyter notebooks](https://jupyter.org/)
2. [IJava](https://github.com/SpencerPark/IJava) Jupyter kernel
3. [Java JDK 1.9+](https://jdk.java.net/) linked to your IJava kernel

##### Importing

When using OWLAPI-Lite with [Jupyter notebooks](https://jupyter.org/) using the [IJava kernel](https://github.com/SpencerPark/IJava), importing the library using ``%maven`` and ``%%loadFromPOM`` [magics](https://github.com/SpencerPark/IJava/blob/master/docs/magics.md) is known to cause problems due to [.ivy2](https://ant.apache.org/ivy/history/2.5.0/settings/caches.html) caching conflicts. The solution is to download the .jar file of OWLAPI-Lite **with packaged dependencies** from the [releases](https://github.com/kodymoodley/simpleowlapi/releases/) section and import this file manually into your IJava notebook by running the ``%jars path/to/jar/file/simpleowlapi-lib-${version}.jar`` command from a single dedicated cell in the notebook.

##### Dependencies

The OWLAPI-Lite release for Jupyter notebooks is bundled into one .jar file for convenience. This file includes the following dependencies (see [pom.xml](https://github.com/kodymoodley/simpleowlapi/blob/master/pom.xml) for specific versions):

1. The [OWL API](https://github.com/owlcs/owlapi) released under both the [GNU Lesser General Public License (LGPL)](https://www.gnu.org/licenses/lgpl-3.0.html) and the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0).
2. The [HermiT](http://www.hermit-reasoner.com/) OWL reasoner released under the [GNU Lesser General Public License (LGPL)](https://www.gnu.org/licenses/lgpl-3.0.html).
3. The [JFact](http://jfact.sourceforge.net/) OWL reasoner released under the [GNU Lesser General Public License (LGPL)](https://www.gnu.org/licenses/lgpl-3.0.html).
4. The [Pellet](https://github.com/stardog-union/pellet) OWL reasoner released under a dual license [GNU Affero](https://www.gnu.org/licenses/agpl-3.0.en.html) for open source usage and under commercial licenses for commercial usage - in the latter case, the [developers](https://github.com/stardog-union/pellet/graphs/contributors) of Pellet need to be contacted.
5. The [ELK](https://github.com/liveontologies/elk-reasoner) OWL reasoner released under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0).
6. The [owlexplanation](https://github.com/matthewhorridge/owlexplanation) library released under the [GNU Lesser General Public License (LGPL)](https://www.gnu.org/licenses/lgpl-3.0.html).

### Usage in Maven projects

Include the following dependency in your pom.xml

```xml
<dependency>
    <groupId>net.sourceforge.simpleowlapi</groupId>
    <artifactId>simpleowlapi-lib</artifactId>
    <version>1.2.1</version>
</dependency>
```

### Examples and documentation

For more information on how to use OWLAPI-Lite, see the [Javadocs](https://kodymoodley.github.io/simpleowlapi/doc/) and examples below.

##### Examples: ontology editing

```java
import org.semanticweb.owl.simpleowlapi.*;
import org.semanticweb.owlapi.model.*;

// create a new SimpleOWLAPIFactory instance which allows the construction and manipulation of OWL ontologies (default OWL reasoner is JFACT)
SimpleOWLAPIFactory s = new SimpleOWLAPIFactory(); 
// create a new OWL ontology by specifying an IRI string and set it to the currently selected (active) ontology
s.createOntology("http://com.kodymoodley/ontologies/2020/testontology#");
// create multiple class names (each separated by a space) and add them to the currently selected ontology
s.createClasses("Penguin Peacock Bird Robin FlyingOrganism Fish Wing Gender Person Female Male");

// change namespace of the editor (temporarily) so we can add or reuse terms from external ontologies or vocabularies
s.setNamespace("http://schema.org/");
// add an object property to the same ontology which will now have the IRI <http://schema.org/parent> (reusing the term from Schema.org)
s.createObjectProperty("parent");

// create multiple object properties (each separated by a space) and add them to the currently selected ontology
s.createObjectProperties("hasPart isPartOf hasGender knows eats hunts");
// create multiple named individuals (each separated by a space) and add them to the currently selected ontology
s.createIndividuals("tweety woody nemo sharky sheila feather1");
// create multiple data properties (each separated by a space) and add them to the currently selected ontology
s.createDataProperties("hasWeight name bornOn"); 

// make an object property transitive
s.makeTransitive("eats"); 
// make an object property symmetric
s.makeSymmetric("knows"); 
// make an object property irreflexive
s.makeIRReflexive("hasGender"); 
// make an object property asymmetric
s.makeAntiSymmetric("hasGender"); 
		
// set whether to render OWL entities (classes, individuals, properties, axioms etc.) using full IRIs or shortform label
s.setFullIRIRendering(true); 

// create an OWL data property assertion axiom and add it to the currently selected ontology
s.createAxiom("tweety Type: hasWeight value \"300.56\"^^xsd:double");
// create an OWL class assertion axiom and add it to the currently selected ontology
s.createAxiom("tweety Type: hasGender exactly 1 Gender"); 
s.createAxiom("tweety Type: Penguin");
// create an OWL object property assertion axiom and add it to the currently selected ontology
s.createObjectPropertyAssertion("woody knows nemo"); 

// create OWL subPropertyOf axiom and add it to the currently selected ontology
s.createAxiom("eats subPropertyOf: hunts"); 
// create inverse object property axiom and add it to the currently selected ontology
s.createAxiom("isPartOf inverseOf: hasPart"); 
// create object property domain axiom and add it to the currently selected ontology
s.createAxiom("knows Domain: Person"); 
// create object property range axiom and add it to the currently selected ontology
s.createAxiom("knows Range: Person"); 

// create OWL subClassOf axiom and add it to the currently selected ontology
s.createAxiom("Penguin subClassOf eats some Fish"); 
s.createAxiom("Penguin subClassOf Bird");
s.createAxiom("Bird subClassOf FlyingOrganism");
s.createAxiom("Penguin subClassOf not FlyingOrganism");
// create OWL equivalent classes axiom and add it to the currently selected ontology
s.createAxiom("Gender equivalentTo: Male or Female"); 
// create OWL equivalent classes axiom and add it to the currently selected ontology
s.createAxiom("Male disjointWith: Female"); 
// create an OWL subClassOf axiom using nominals and add it to the currently selected ontology
s.createAxiom("{tweety,woody} subClassOf Bird"); 

s.createOntology("http://com.kodymoodley/ontologies/2020/testontology2#");
// set or switch the "active" ontology by specifying the IRI of the ontology to switch to
s.setOntology("http://com.kodymoodley/ontologies/2020/testontology2#");
// prints to console the IRI of the currently selected ontology
s.getOntology(); 
// prints to console the IRIs of all ontologies created / loaded within the current context (instance of the simpleOWLAPIFactory)
s.getOntologies(); 
// prints to console a structured representation of the main OWL entities in the active ontology
s.printOntology();
// prints active ontology metrics (e.g. number of classes, axioms of a certain type etc.)
s.printOntologyStats(); 

// save active ontology to local file using Manchester OWL syntax
s.saveOntology("testontology.owl"); 
// load an ontology into this context from a remote URL
s.loadFromURL("https://protege.stanford.edu/ontologies/pizza/pizza.owl"); 
// load an ontology into this context from a local file path. WARNING: you cannot load multiple ontologies with the same IRI into the same context!
s.loadFromFile("path/to/ontology.owl");	
// remove selected ontology from current context (simpleOWLAPIFactory instance) 
s.removeOntology(); 
// remove ontology with specified IRI from current context (simpleOWLAPIFactory instance) 
s.removeOntology("http://com.kodymoodley/ontologies/2020/testontology2#"); 

// remove all axioms from the currently selected ontology
s.resetOntology(); 
// remove multiple class names from currently selected ontology
s.removeClasses("Bird Penguin"); 
// remove multiple object properties from currently selected ontology
s.removeObjectProperties("knows hasPart"); 
// remove multiple data properties from currently selected ontology
s.removeDataProperties("hasWeight name"); 
// remove multiple individual names from currently selected ontology
s.removeIndividuals("tweety woody"); 
// remove an axiom from the currently selected ontology
s.removeAxiom("Penguin subClassOf eats some Fish"); 

```

##### Examples: reasoning

```java
SimpleOWLAPIFactory s = new SimpleOWLAPIFactory();

...

// Switch or set OWL reasoner 
s.setOWLReasoner(SelectedReasoner.HERMIT); 

// prints to console Yes if currently selected ontology is consistent, No otherwise
s.owlReasoner.isConsistent(); 
// computes and prints to console all explanations for the inconsistency of the selected ontology (provided it is inconsistent)
s.owlReasoner.explainInconsistency(); 

// prints to console Yes if the given class expression is satisfiable, No otherwiseNo otherwise
s.owlReasoner.isSatisfiable("Penguin");
// computes and prints to console all explanations for the unsatisfiability of the given class expression w.r.t. the active ontology (provided it is indeed unsatisfiable)
s.owlReasoner.explainUnsatisfiability("Penguin"); 

// prints to console Yes if the given axiom is entailed by the currently selected ontology, No otherwise
s.owlReasoner.isEntailed("Robin subClassOf FlyingOrganism"); 
// computes and prints to console all explanations for the entailment of the given axiom w.r.t. the selected ontology (provided it is indeed entailed)
s.owlReasoner.explainEntailment("Robin subClassOf FlyingOrganism"); 

// computes and prints to console all super classes (indirect) for the given class expression
s.owlReasoner.getSuperClasses("Robin");	
// computes and prints to console all sub classes (indirect) for the given class expression
s.owlReasoner.getSubClasses("FlyingOrganism"); 
// computes and prints to console all atomic classes equivalent to the given class expression
s.owlReasoner.getEquivalentClasses("Robin and not FlyingOrganism);

// computes and prints to console all class names for which the given individual is an instance
s.owlReasoner.getTypes("tweety"); 
// for each individual name in the selected ontology, computes and prints to console all class names such that this individual is an instance of the class name
s.owlReasoner.getAllTypes(); 
// computes and prints to console all instances of the given class expression
s.owlReasoner.getInstances("Robin and not FlyingOrganism); 

// given an object property name R, prints to console all individual name pairs (a,b) such that R(a, b) is an object property assertion entailed by the selected ontology
s.owlReasoner.getObjectPropertyAssertions("knows"); 
// for each object property R in the ontology, prints to console all individual name pairs (a,b) such that R(a, b) is an object property assertion entailed by the active ontology
s.owlReasoner.getAllObjectPropertyAssertions(); 

// prints to console the name of the OWL reasoner which is currently being used by the simpleOWLAPIFactory instance
s.owlReasoner.getName(); 
// prints to console the name of the OWL 2 profile which the selected OWL reasoner supports
s.owlReasoner.getOWLProfile(); 

```
### For Developers: Building the library from source

##### Requirements:

+ Apache's [Maven](http://maven.apache.org/index.html).
+ A tool for checking out a [Git](http://git-scm.com/) repository.

##### Steps:

1. Get a copy of the code:

        git clone https://github.com/kodymoodley/owlapi-lite.git
    
2. Change into the `owlapi-lite/` directory.

3. Type `mvn clean package`.  On build completion, the `target/` directory will contain two versions of the library: `simpleowlapi-lib-${version}.jar` and `simpleowlapi-lib-${version}-jar-with-dependencies.jar`.

### License and contributions
The OWLAPI-Lite library is copyrighted by [Kody Moodley](https://sites.google.com/site/kodymoodley/) and released under the [GNU Affero License](https://github.com/kodymoodley/simpleowlapi/blob/master/LICENSE.md).

Contributions and bug reports are helpful and welcome.
