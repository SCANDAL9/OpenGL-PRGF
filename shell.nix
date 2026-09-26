{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  buildInputs = with pkgs; [
    jdk21
    maven
    libX11
    libXext
    libXcursor
    libXrandr
    libXxf86vm
    libGL
    openal-soft
  ];

  shellHook = ''
    export JAVA_HOME="${pkgs.jdk21}"
    export PATH="$JAVA_HOME/bin:$PATH"
    export LD_LIBRARY_PATH="${pkgs.lib.makeLibraryPath (with pkgs; [
      libX11 libXext libXcursor libXrandr libXxf86vm libGL openal-soft
    ])}''${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"
    export GLFW_USE_WAYLAND=0
  '';
}
