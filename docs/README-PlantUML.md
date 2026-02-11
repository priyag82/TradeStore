# Trade Store System - PlantUML Diagrams

This directory contains PlantUML diagrams that visualize the Trade Store system architecture and processing flow.

## 📊 Diagrams Overview

### 1. Trade Processing Sequence Diagram
**File**: `trade-sequence-diagram.puml`

Shows the complete flow of a trade message from Kafka through validation to persistence in both PostgreSQL and MongoDB.

**Key Components:**
- Kafka Topic (`incoming.trade.data`)
- KafkaTradeConsumer
- TradeService with validation
- PostgreSQL (TradeRepository)
- MongoDB (AuditMessageRepository)

**Flow Steps:**
1. Kafka message reception
2. JSON parsing and conversion
3. Trade validation
4. PostgreSQL persistence
5. MongoDB audit trail creation
6. Message acknowledgment

### 2. Trade System Class Diagram
**File**: `trade-class-diagram.puml`

Illustrates the relationships between entities, services, and repositories in the trade system.

**Key Packages:**
- `com.tradestore.entity` - Trade and AuditMessage entities
- `com.tradestore.dto` - TradeMessage data transfer object
- `com.tradestore.service` - Business logic components
- `com.tradestore.repository` - Data access interfaces
- `org.springframework.data` - Spring Data base interfaces

**Important Relationships:**
- TradeRepository extends JpaRepository (PostgreSQL)
- AuditMessageRepository extends MongoRepository (MongoDB)
- TradeService orchestrates trade processing
- KafkaTradeConsumer handles message consumption
- TradeExpiryScheduler manages automated trade expiry

## 🛠️ How to Generate Diagrams

### Using PlantUML Online (Recommended)
1. Go to [PlantUML Online Server](http://www.plantuml.com/plantuml)
2. Copy the contents of the `.puml` file
3. Paste into the online editor
4. Click "Generate" to view the diagram

### Using PlantUML CLI
```bash
# Install PlantUML
# For macOS: brew install plantuml
# For Ubuntu: sudo apt-get install plantuml
# For Windows: Download from https://plantuml.com/download

# Generate PNG diagram
plantuml -tpng trade-sequence-diagram.puml

# Generate SVG diagram (vector format)
plantuml -tsvg trade-class-diagram.puml

# Generate all diagrams
plantuml -tpng *.puml
```

### Using VS Code
1. Install the "PlantUML" extension from the marketplace
2. Open a `.puml` file
3. Use the command palette (Ctrl+Shift+P) and search for "PlantUML: Preview"
4. Or use the preview button in the top-right corner

### Using IntelliJ IDEA
1. Install the "PlantUML integration" plugin
2. Right-click on a `.puml` file
3. Select "Show PlantUML Diagram"

## 📋 Diagram Features

### Sequence Diagram Highlights:
- **Async Processing**: Shows Kafka consumer acknowledgment flow
- **Error Handling**: Alternate flows for validation failures
- **Audit Trail**: MongoDB audit message creation process
- **Transaction Boundaries**: Clear service layer boundaries

### Class Diagram Highlights:
- **Spring Annotations**: All relevant Spring annotations are documented
- **Inheritance Hierarchy**: Shows Spring Data repository extensions
- **Package Organization**: Clear separation of concerns across packages
- **Dependency Direction**: Arrows show dependency relationships

## 🔧 Customization Tips

### Adding New Components
To add new components to the diagrams:

1. **Sequence Diagram**: Add new participants and message flows
```plantuml
participant NewService as "New Service"
Service -> NewService : invokeMethod()
```

2. **Class Diagram**: Add new classes and relationships
```plantuml
class NewClass {
  - field1
  - field2
  --
  + method1()
  + method2()
}

NewClass --> ExistingClass : uses
```

### Styling Options
PlantUML supports various styling options:
- **Colors**: Use skinparam commands
- **Fonts**: Customize text styles
- **Layout**: Adjust diagram orientation and spacing

Example:
```plantuml
skinparam class {
  BackgroundColor LightBlue
  ArrowColor Navy
  BorderColor DarkBlue
}
```

## 📱 Export Formats

PlantUML supports multiple export formats:
- **PNG**: Raster image, good for documents
- **SVG**: Vector image, good for web
- **PDF**: Vector document, good for printing
- **LaTeX**: For academic papers
- **HTML**: Interactive web diagrams

## 🐛 Troubleshooting

### Common Issues:
1. **Syntax Errors**: Check PlantUML syntax carefully
2. **Missing Dependencies**: Ensure PlantUML is installed correctly
3. **Font Issues**: Use default fonts for compatibility
4. **Large Diagrams**: Break down complex diagrams into smaller ones

### Validation:
- Use PlantUML online validator to check syntax
- Test with simple diagrams first
- Check PlantUML version compatibility

## 📚 Additional Resources

- [PlantUML Official Documentation](https://plantuml.com/)
- [PlantUML Language Reference](https://plantuml.com/starting)
- [Spring Boot Architecture Best Practices](https://spring.io/guides/gs/rest-service/)

## 🔄 Version Control

These diagrams are version-controlled with the codebase. When making changes:
1. Update the `.puml` files
2. Regenerate the diagrams
3. Commit both source and generated images
4. Update this README if needed

---

*Last updated: February 2026*
