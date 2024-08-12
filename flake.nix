{
  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?rev=9957cd48326fe8dbd52fdc50dd2502307f188b0d";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils, fenix }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
      in
      {
        devShells.default = pkgs.mkShell {
          
          buildInputs = with pkgs; [
            jdk
            gradle
            postgresql
          ];

          JAVA_HOME = "${pkgs.jdk.home}";
        };
      });
}
