WIP

[![zh-CN](https://img.shields.io/badge/lang-中文-blue)](README.zh-CN.md)

# Pure ORM Design Philosophy
## I. Core Positioning of the Framework
Pure ORM adheres to the core philosophy of **minimalism, purity, transparency, and controllability**, completely stripping away all redundant designs of traditional persistence layer frameworks and returning to the most original and essential core responsibilities of ORM.

It only focuses on two core capabilities: **constructing SQL based on Lambda type-safe coding** and **achieving fully automatic mapping between database results and entity objects based on Lambda type safety**.

It abandons over-encapsulation, complex abstraction, and non-core functions, does not pursue full dialect compatibility or redundant specification coverage, and focuses on solving real pain points in production environments. It is lightweight, low-memory, easy to debug and maintain, truly achieving simplicity, practicality, and high efficiency.

Meanwhile, the framework **supports native SQL writing**, retaining the most flexible capability of customizing SQL. All native SQL can enjoy fully automatic mapping without coercion, restriction, or abandonment of native syntax, meeting the needs of extremely complex scenarios. Daily development does not require writing redundant native SQL, balancing flexibility and efficiency.

## II. Comprehensive Abandonment of the Drawbacks of Traditional Frameworks
### 1. Abandon Over-abstraction, Deep Encapsulation, and Black-box Operation
Completely discard the inferior design of traditional ORM frameworks characterized by **extremely deep hierarchies, over-abstraction, and high encapsulation**.

The underlying logic of traditional ORM frameworks is complex, nested, and the execution process is fully closed. Developers cannot view, track, or debug with breakpoints, leading to difficulties in locating online problems and high maintenance costs.

Pure ORM features fully visible code, transparent logic, no hidden logic, no underlying proxies, and no complex nesting. Every line of SQL and every mapping step is traceable and debuggable, completely bidding farewell to black-box development.

### 2. Abandon Multi-level Cache and Built-in Cache Coupling Design
Directly remove all redundant designs such as built-in first-level cache, second-level cache, and global cache in the framework.

Traditional frameworks come with built-in caching, which easily causes data inconsistency, cache invalidation, dirty data, and query exceptions. Moreover, caching is strongly coupled with the core logic of ORM, making problems difficult to troubleshoot and logic hard to control.

Pure ORM adheres to the separation of responsibilities: **caching is implemented by professional caching frameworks**. ORM only focuses on data mapping, without coupling, redundancy, or implicit issues.

### 3. Abandon Lazy Loading, Cascade Loading, and Cascade Operations
Completely remove all unstable designs such as lazy loading, one-to-many cascading, many-to-many cascading, and implicit queries.

Traditional frameworks automatically trigger cascading queries and lazy loading SQL, which easily leads to performance disasters, memory overflow, and uncontrollable queries, with frequent exceptions in front-end and back-end separation architectures.

Pure ORM has no implicit queries, no automatic associations, and no lazy loading. All query logic is explicitly controlled by developers, ensuring stability, security, and no unexpected behavior.

### 4. Abandon XML Configuration, String SQL, and HQL Syntax
Abandon the massive XML configuration and separation of SQL from code in MyBatis; abandon HQL strings in Hibernate and complex annotations in JPA.

Say goodbye to scattered configurations, hard-coded SQL, type insecurity, no compilation errors, and errors only occurring at runtime.

Development is fully based on **Lambda expressions** for coding, featuring type safety, compilation checks, no strings, and no configuration files, making development more robust and efficient.

### 5. Abandon Bulky and Redundant Functions, and Non-essential Extended Capabilities
Abandon all useless functions such as complex transaction encapsulation, complex dialect compatibility, complex life cycles, and complex proxy objects.

Traditional frameworks are large and heavy, with excessive functions, high memory usage, slow startup, and bloated container deployment.

Pure ORM only retains core capabilities, being extremely lightweight with no redundant objects or logic, running efficiently and being memory-friendly, adapting to cloud-native, microservice, and high-concurrency scenarios.

### 6. Abandon Complex Many-to-Many Mapping and Redundant Entity Design
Traditional frameworks support complex many-to-many mapping, which involves cumbersome configuration, chaotic logic, uncontrollable queries, and easily leads to redundant SQL and logical vulnerabilities.

Pure ORM only retains **one-to-one and one-to-many** standard association mappings. Many-to-many relationships are simplified by joining intermediate tables through SQL statements, resulting in a clean and concise entity layer with no complex configurations or mapping traps.

## III. Clean Design Principles of Pure ORM Itself
1. Full Lambda coding, type-safe, no string SQL, no redundant configuration
2. Support for native SQL with fully automatic result mapping, balancing flexibility and efficiency
3. Native support for production-essential syntax: select for update row lock, upsert insert or update
4. No black boxes, no black magic, no implicit logic, fully traceable and maintainable throughout
5. Only perform core ORM tasks, with no redundancy, bloat, coupling, or intrusion
6. Simple and easy to understand, low cost to get started, stable online, and easy to troubleshoot

## IV. Summary of Overall Design
Pure ORM is not about piling up functions, but about **doing subtraction, eliminating drawbacks, and retaining essence**.

It eliminates all complex, bloated, uncontrollable, hard-to-debug, and highly coupled designs of traditional ORM, retaining only the cleanest, core, and most practical capabilities.

With an extremely simple architecture, transparent logic, and coding-based development, it truly achieves light weight, stability, high efficiency, and controllability, making it a **pure native ORM framework** adapted to modern development architectures.
