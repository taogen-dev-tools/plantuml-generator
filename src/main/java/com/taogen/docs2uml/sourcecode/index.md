# Generating PlantUML Text from Source Code

## Difficulties

1\. Scanning files

- None

2\. Parsing files to myEntities

- Regex need to satisfy all cases of Java source code.
- Handling strings contains generic strings.
- Parse parent class and parent interfaces of entities.

3\. Filtering specified classes

- Gets the class of the graph structure type.

4\. Generating PlantUML text

- Using the previous code.
