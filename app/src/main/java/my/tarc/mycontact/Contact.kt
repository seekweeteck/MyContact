package my.tarc.mycontact

data class Contact (val name: String, val phone: String) {
    override fun toString(): String {
        return "$name : $phone"
    }
}
