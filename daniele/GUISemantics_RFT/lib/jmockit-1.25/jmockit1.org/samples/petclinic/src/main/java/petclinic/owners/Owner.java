package petclinic.owners;

import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.*;

import petclinic.pets.*;
import petclinic.util.*;

/**
 * A pet owner.
 */
@Entity
public class Owner extends Person
{
   private String address;
   private String city;

   @NotNull
   private String telephone;

   @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
   @OrderBy("name")
   private final List<Pet> pets = new ArrayList<>();

   public String getAddress() { return address; }
   public void setAddress(String address) { this.address = address; }

   public String getCity() { return city; }
   public void setCity(String city) { this.city = city; }

   public String getTelephone() { return telephone; }
   public void setTelephone(String telephone) { this.telephone = telephone; }

   public List<Pet> getPets() { return pets; }

   public void addPet(Pet pet)
   {
      pets.add(pet);
      pet.setOwner(this);
   }

   /**
    * Return the Pet with the given name, or null if none found for this Owner.
    */
   public Pet getPet(String name)
   {
      for (Pet pet : pets) {
         if (pet.getName().equalsIgnoreCase(name)) {
            return pet;
         }
      }

      return null;
   }
}
