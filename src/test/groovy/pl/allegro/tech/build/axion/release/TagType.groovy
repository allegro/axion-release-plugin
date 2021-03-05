package pl.allegro.tech.build.axion.release

enum TagType {
    PREFIX_SEPARATOR("v-")

    TagType(String value) {
        this.value = value
    }

    private final String value

    String prefix(){
        return PREFIX_SEPARATOR
    }
}
