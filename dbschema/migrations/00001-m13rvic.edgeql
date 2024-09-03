CREATE MIGRATION m13rvicvcyuuckmg5t7xknqgw4d22p53xkpzgbzax6y72y7tf6a6pq
    ONTO initial
{
  CREATE TYPE default::Position {
      CREATE REQUIRED PROPERTY x: std::int32;
      CREATE REQUIRED PROPERTY y: std::int32;
  };
  CREATE TYPE default::Carrot {
      CREATE LINK position: default::Position;
  };
  CREATE TYPE default::Figure {
      CREATE LINK position: default::Position;
      CREATE REQUIRED PROPERTY name: std::str;
      CREATE REQUIRED PROPERTY score: std::int32;
      CREATE REQUIRED PROPERTY stat_moves: std::int32;
  };
  CREATE TYPE default::Fox {
      CREATE REQUIRED LINK figure: default::Figure;
  };
  CREATE TYPE default::Rabbit {
      CREATE REQUIRED LINK figure: default::Figure;
      CREATE REQUIRED PROPERTY nutrition: std::int32;
  };
};
