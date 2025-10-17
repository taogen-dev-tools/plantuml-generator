# Generating PlantUML Text from Source Code

## Difficulties

1\. Scanning files

- None

2\. Parsing files to myEntities

- Regex need to satisfy all cases of Java source code. Continuously analyze text, update regex, and try regex.
- Handling strings contains complex generic strings. For example, `DynamicFileAssert<A extends DynamicFileAssert<A, F>, F extends DynamicFile>`.
- Parse parent class and parent interfaces of entities.

3\. Filtering specified classes

- Gets the class of the graph structure type.

4\. Generating PlantUML text

- Using the previous code.
