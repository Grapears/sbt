<script setup lang="ts">
import {ref, computed} from 'vue'
import VInput from '@/components/atoms/VInput.vue';
import VButton from '@/components/atoms/VButton.vue'
import VTextFormAnchor from '@/components/atoms/VTextFormAnchor.vue'

const emailModelValue = ref("")
const passwordModelValue = ref("")
const regexEmail = /^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/;

const props = defineProps<{
  success: string,
  errors: string
}>()

const emits = defineEmits<{
  (event: "click", email: string, password: string): void,
  (event: 'update:modelValue', value: string): void,
  (event: 'googleClick'): void
}>()

const onClickHandler = () => {
  emits("click", emailModelValue.value, passwordModelValue.value)
}

const isEmailValid = computed(() => {
  const email = emailModelValue.value;
  return regexEmail.test(email)

})

</script>

<template>
  <form class="loginForm" @submit.prevent="emits('click', emailModelValue, passwordModelValue)">
      <VInput v-model="emailModelValue" class="emailInput" type="text" placeholder="Email"/>
      <span class="errors" v-if="!isEmailValid && emailModelValue">
        <img src='@/assets/Error_icon_logo.svg' alt="error logo" class="error-icon">
        &nbsp;Please input a valid email
      </span>
      <VInput v-model="passwordModelValue" class="passwordInput" type="password" placeholder="Password" />
      <!-- <p class="errors" v-if="props.errors"><img src='@/assets/Error_icon_logo.svg' alt="error logo" class="error-icon">&nbsp{{ props.errors }}</p> -->
      <VTextFormAnchor href="/forgotPassword" class="forgotPassword">
          Forgot password?
      </VTextFormAnchor>
      <VButton class="loginButton button-1" type="submit"> 
          Login
      </VButton>
      <!-- <p class="success" v-if="props.success">{{ props.success }}</p> -->
      <VTextFormAnchor class="signupAnchor" href="/register" >
          Don't have an account? <span class="signUp">Sign up now!</span>
      </VTextFormAnchor>

      <div class="orLoginWith">
        <hr />
        or
        <hr />
      </div>
      <div class="google-signup button-1" @click="emits('googleClick')">
          <img src='@/assets/Google_G_logo.svg' alt="Google logo">
          Login with Google

      </div>
  </form>
</template>

<style scoped>
/**{
  outline: 1px solid black;
}
*/
.google-signup {
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px solid #ccc;
  padding: 10px;
  cursor: pointer;
  border-radius: 4px;
  width: 100%;
}

.google-signup img {
  margin-top: 2px;
  width: 24px;
  height: 24px;
  margin-right: 10px;
}

.error-icon{
  margin-top: 1px;
  width: 13px;
  height: 14px;
}

.signUp:hover {
  text-decoration: underline;
}

.signUp {
    font-weight: 600;
}
.loginForm{
  display: flex;
  align-items: center;
  flex-direction: column;
  position: relative;
  gap: 27px;
}

.forgotPassword:hover{
  text-decoration: underline;
}
.forgotPassword{
  margin: 0px 10px 0 -220px
}
.loginButton{
  margin-top: -5px;
  padding:  0px;
}
.signupAnchor{
  margin-top: -15px;
}
table tr td{
  border: 1px solid black;
}
table{
  width: 100px;
}
hr{
  width: 145px;
}
.orLoginWith{
  display: flex;
  justify-content: center;
  align-items:center;
  gap: 10px;
  font-size: 13px;
  letter-spacing: 0px;
  line-height: auto;
}
img{
  height: 40px;
  margin-top: -10px;
}

.errors {
  width: 100%;
  display: flex;
  align-items: flex-start;
  color: var(--color5);
  font-size: 13px;
  font-weight: 300;
  line-height: auto;
  letter-spacing: 0px;
  margin: -23px 0;
}

.googleBtn{
  border: none;
}

.success {
  text-align: center;
  color: green;
  margin-top: -20px;
  margin-bottom: -10px;
}
</style>
---------------
<script setup lang="ts">
import {ref} from 'vue'
import VLoginTitle from '@/components/molecules/VLoginTitle.vue'
import RegisterForm from '@/components/molecules/RegisterForm.vue'
import { useCheckUserStore } from '@/stores/checkUser'
import axiosInstance from '@/axiosInstance';
import { useAuthenticationStore } from '@/stores/authentication'
import { googleSdkLoaded } from 'vue3-google-login'
import {storeToRefs} from 'pinia'
import { toast } from 'vue3-toastify';

const { checkUser, fetchUser } = useCheckUserStore()
const userStore = useAuthenticationStore()
const { fetchUserDataFrom, logoutGoogle, isGoogleUserRegistered } = userStore
const { clientId } = storeToRefs(userStore)

const username = ref('');
const password = ref('');
const confirmPassword = ref('');
const errors = ref('');
const success = ref('');
const regexEmail = /^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/;
const regexPassword= /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/
const emit = defineEmits(['register-user']);

const registerUser = async (username: string, password: string, confirmPassword: string) => {
    
    if (!regexEmail.test(username)) {
        return;
    }

    if (password !== confirmPassword) {
        return;
    }

    const userData = {
        emailUser: username,
        passwordUser: password
    };


    try {
          const response = await axiosInstance.post('http://localhost:9000/api/users/register', userData);
          const data = await response.data;

          if (response.status === 200){
            success.value = 'Successful Registration!'
            await fetchUser()
            errors.value = ''
            toast.success("Successful Registration!", {
              position: 'bottom-right',
              autoClose: 1500,
            });
            setTimeout(() => {
              window.location.href = '/login';
            }, 1500);
          }

    } catch (error: any) {
        errors.value = error.response.data.error;
        success.value = ''
        toast.error(errors.value, {
          position: 'bottom-right',
          autoClose: 3000,
        });
    }



}

const signInWithGoogle1 = async () => {
  const isRegistered = await isGoogleUserRegistered();
  if (isRegistered) {
    window.alert("User already registered");
  } else {
    googleSdkLoaded(google => {
      google.accounts.oauth2
        .initCodeClient({
          client_id: clientId.value,
          scope: 'email profile openid',
          redirect_uri: "http://localhost:5173/",
          callback: response => {
            if (response.code)
              fetchUserDataFrom(response.code)
          },
        })
        .requestCode()
    })
  }
};

const signInWithGoogle = async () => {
  googleSdkLoaded(google => {
    google.accounts.oauth2
      .initCodeClient({
        client_id: clientId.value,
        scope: 'email profile openid',
        redirect_uri: "http://localhost:5173/",
        callback: response => {
          if (response.code)
            fetchUserDataFrom(response.code)
        },
      })
      .requestCode()
  })
};

</script>

<template>
  <div class="login-organisms">
    <div class="left-line">
      <VLoginTitle />
    </div>
    <div class="right-line">
      <RegisterForm :errors="errors" :success="success" @click="registerUser" @googleClick="signInWithGoogle"/>
    </div>

  </div>
</template>

<style scoped>
*{
  /* outline: 1px solid black; */
}
.login-organisms{
  margin-top: 100px;
  margin-bottom: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.right-line{
  border-left: 1px solid black;
  padding: 0 100px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.left-line{
  padding: 0 100px;
}

.errors {
  color: red;
}

@media(max-width: 970px){
  .login-organisms{
    flex-direction: column;
  }
  .left-line{
    margin-bottom: 1em;
  }
  .right-line{
    border-left: none;
  }
}
</style>
-------------------
<script setup lang="ts">
import { ref, watch } from 'vue'
import { useChecklistItemStore } from '@/stores/checklistItem'

const checklistItemStore = useChecklistItemStore();
const { updateChecklistItemCheckbox, updateSingleChecklistItemCheckbox } = checklistItemStore

interface ChecklistItem {
  idChecklistItem: string;
  checklistsIdChecklistItem: string;
  parentIdChecklistItem?: string;
  descriptionChecklistItem: string;
  isCheckedChecklistItem: boolean;
  levelChecklistItem: number;
  updatedByChecklistItem: string;
}

const props = defineProps<{
  checklistItem: ChecklistItem
}>()

const emits = defineEmits<{
  (event: 'addChecklistItemAttemptHandler', checklistsIdChecklistItem: string, idChecklistItem: string, levelChecklistItem: number): void,
  (event: 'updateChecklistItemAttemptHandler', idChecklistItem: string, checklistsIdChecklistItem: string, parentIdChecklistItem?: string, descriptionChecklistItem?: string): void,
  (event: 'deleteChecklistItemAttemptHandler', checklistItemId: string, checklistsIdChecklistItem: string, parentIdChecklistItem?: string, descriptionChecklistItem?: string): void
}>()

const isCheckedRef = ref(props.checklistItem.isCheckedChecklistItem)
const emailUser = localStorage.getItem('emailUser')

async function checkChecklistItemCheckbox(checklistItemId: string, isChecked: boolean, checklistsIdChecklistItem: string) {
  try {
    await updateSingleChecklistItemCheckbox(checklistItemId, isChecked, checklistsIdChecklistItem)
  } catch (error) {
    // console.error('Error updating checklist item checkbox:', error);
    isCheckedRef.value = !isCheckedRef.value;
  }
}

watch(() => props.checklistItem.isCheckedChecklistItem, (newVal) => {
  isCheckedRef.value = newVal;
});
</script>

<template>
    <div class="d-flex align-items-center gap-05" :style="{marginLeft: props.checklistItem && props.checklistItem.levelChecklistItem * 20 + 'px'}">
        <input 
            type="checkbox" 
            :disabled="props.checklistItem.updatedByChecklistItem !== null && 
                props.checklistItem.updatedByChecklistItem !== emailUser"
            v-model="isCheckedRef" 
            @change="checkChecklistItemCheckbox(props.checklistItem.idChecklistItem, 
                isCheckedRef, 
                props.checklistItem.checklistsIdChecklistItem)"
            >
        <label :class="{'line-through': isCheckedRef}">{{ props.checklistItem.descriptionChecklistItem }}</label>
        <div class="align-items-center d-flex">
            <button 
                class="button-3 p-02"
                @click="emits(
                            'addChecklistItemAttemptHandler', 
                            props.checklistItem.checklistsIdChecklistItem,
                            props.checklistItem.idChecklistItem,
                            props.checklistItem.levelChecklistItem
                        )">
                            <img src="@/assets/add-circle.svg" alt="add checklist item">
                        </button>
            <button 
                class="button-3 p-02"
                @click="emits(
                                'updateChecklistItemAttemptHandler',
                                props.checklistItem.idChecklistItem,
                                props.checklistItem.checklistsIdChecklistItem,
                                props.checklistItem.parentIdChecklistItem,
                                props.checklistItem.descriptionChecklistItem
                            )"><img src="@/assets/update-checklist-item.svg"></button>
            <button 
                class="button-3 p-02"
                @click="emits(
                                'deleteChecklistItemAttemptHandler',
                                props.checklistItem.idChecklistItem,
                                props.checklistItem.checklistsIdChecklistItem,
                                props.checklistItem.parentIdChecklistItem,
                                props.checklistItem.descriptionChecklistItem
                            )"><img src="@/assets/delete-checklist-item.svg"></button>
            <i 
                v-if="props.checklistItem.updatedByChecklistItem"
                class="text-color-4 pl-05" 
            >
                Checkbox updator: {{props.checklistItem.updatedByChecklistItem}}
            </i>     
        </div>           
    </div>
</template>
---------------------
<script setup lang="ts">
  import { ref, onMounted, onUpdated } from 'vue'
  import { useUserStore } from '@/stores/user'
  import VChecklistChangeModal from '@/components/atoms/VChecklistChangeModal.vue'

  interface Checklist {
    idChecklist: string;
    nameChecklist: string;
    idUserChecklist: string;
    isCheckedChecklist: boolean;
    createdAtChecklist: number;
    hasMembersChecklist: boolean
  }

  const props = defineProps<{
    checklist: Checklist,
    index: number,
    checklistArrIndexRef?: number
  }>()
  const emits = defineEmits<{
    (e: 'checklistOnClick'): void
  }>()
  const userStore = useUserStore()
  const { getUserEmailById } = userStore

  const userEmail = ref('')
  const showChecklistChangeModal = ref(false)

  function toggleChecklistChangeModal() {
    showChecklistChangeModal.value = !showChecklistChangeModal.value
  }
  
  //balikonon ni para sa member admin change
  onMounted(async () => {
    // console.log(props.checklist)
    if(props.checklist && props.checklist.idUserChecklist)
    userEmail.value = await getUserEmailById(props.checklist.idUserChecklist)
  })
  onUpdated(async () => {
    if(props.checklist && props.checklist.idUserChecklist)
    userEmail.value = await getUserEmailById(props.checklist.idUserChecklist)
  })
</script>
<template>
  <div 
    :class="['check-list-card', 
      'flex-col', 
      'gap-05', 
      {'card-selected': props.checklistArrIndexRef === props.index}
    ]" 
    @click="emits('checklistOnClick')"
  >
    <div class="space-between align-items-center relative">
      <label v-if="props.checklist && props.checklist.nameChecklist">
        {{props.checklist.nameChecklist}}
      </label>
      <!-- <button class="button-2" @click.stop="toggleChecklistChangeModal">
        <img 
          class="ellipsis"
          src="@/assets/ellipsis.svg" 
          height="25" 
          width="25"
        >
      </button> -->
      <!-- <VChecklistChangeModal 
        v-if="showChecklistChangeModal"
        @editChecklistItemAttemptHandler="editChecklistItemAttemptHandler"
        @deleteChecklistItemAttemptHandler="deleteChecklistItemAttemptHandler"
        @toggleShowChecklistChangeModal="toggleChecklistChangeModal"
        :id="props.index"
      /> -->
    </div>
    <div class="d-flex space-between align-items-center">
      <img 
        v-if="props.checklist.hasMembersChecklist"
        src="@/assets/people.svg" width=15 height=15
      >
      <div v-else></div>
      <div 
        :class="['w-fit', 
          'border-1', 
          'border-radius-1', 
          'pl-05', 
          'pr-05', 
          'user-email', 
          {'w-150': userEmail.length > 15}]"
      >{{userEmail}}</div>
    </div>
  </div>
</template>
<style scoped>
  @media (max-width: 768px){
    .check-list-card{
      border-radius: 0;
      border: none;
      border-bottom: 1px solid var(--color2);
      padding: 1em;
      padding-left: 1.3em;
    }
    .ellipsis{
      width: 40px;
    }
  }
</style>
-----------------
<script setup lang="ts">
  import VInput from '@/components/atoms/VInput.vue'
  import { onMounted } from 'vue'
  import { useMembersStore } from '@/stores/members'
  const props = withDefaults(defineProps<{
    isAddChecklist?: boolean,
    isAddChecklistItem?: boolean,
    isUpdateChecklistItem?: {
      show: boolean,
      data: string
    },
    isDeleteChecklistItem?: boolean,
    isAddChecklistMember?: boolean,
    descriptionRef?: string,
    checklistItem?: object,
    userAddResponse?: string
  }>(),{
    isAddChecklist: false,
    isAddChecklistItem: false,
    isDeleteChecklistItem: false,
    isAddChecklistMember: false,
    isUpdateChecklistItem: () => ({
      show: false,
      data: ''
    })
  })
  const emits = defineEmits([
    'closeModal', 
    'addChecklist', 
    'addChecklistItem',
    'updateChecklistItem',
    'deleteChecklistItem',
    'addChecklistMember'
  ])
  const modelInput = defineModel()
  const membersStore = useMembersStore()
  const { fetchMembersExceptMe } = membersStore
  onMounted(async () => {
    document.getElementById("modalInput")! && document.getElementById("modalInput")!.focus()
    if(props.isAddChecklistMember) {
      await fetchMembersExceptMe()
    }
  })
</script>
<template>
  <div class="modal-back-drop">
    <div class="modal border-radius-2">
      <form v-if="isAddChecklist" @submit.prevent="emits('addChecklist')">
        <!-- <div class="d-flex space-between align-items-center">
          <p>Create checklist</p>
          <img src="@/assets/close.svg" width=40 height=40>
        </div>
        <div>
          <label for="checklistNameInput">Name: </label>
          <VInput v-model="modelInput" type="text" />
        </div>
        <p>Checklist items:</p>
        <div>
          
        </div> -->
        <div class="d-flex space-between align-items-center mb-1">
          <p>Add checklist</p>
          <button 
            type="button" 
            class="button-2 close-btn"
            @click="emits('closeModal')"
          >
            <img class="close-img" src="@/assets/close.svg" width=30 height=30 alt="close modal">
          </button>
        </div>
        <div class="d-flex gap-05 align-items-center">
          <label for="modalInput">Name: </label>
          <input type="text" id="modalInput" v-model.trim="modelInput" required>
          <button class="button-1 pl-1 pr-1">Add</button>
        </div>
      </form>
      <form 
        v-else-if="isAddChecklistItem" 
        @submit.prevent="emits('addChecklistItem', modelInput)">
        <div class="d-flex space-between align-items-center mb-1">
          <p>Add Checklist Item</p>
          <button 
            type="button" 
            class="button-2 close-btn"
            @click="emits('closeModal')"
          >
            <img class="close-img" src="@/assets/close.svg" width=30 height=30 alt="close modal">
          </button>
        </div>
        <div class="d-flex gap-05 align-items-center">
          <label for="modalInput">Item: </label>
          <input type="text" id="modalInput" v-model.trim="modelInput" required>
          <button class="button-1 pl-1 pr-1">Add</button>
        </div>
      </form>
      <form 
        v-if="isUpdateChecklistItem!.show"
        @submit.prevent="emits('updateChecklistItem', props.isUpdateChecklistItem!.data)">
        <div class="d-flex space-between align-items-center mb-1">
          <p>Update Checklist Item</p>
          <button 
            type="button" 
            class="button-2 close-btn"
            @click="emits('closeModal')"
          >
            <img class="close-img" src="@/assets/close.svg" width=30 height=30 alt="close modal">
          </button>
        </div>
        <div class="d-flex gap-05 align-items-center">
          <label for="modalInput">Item: </label>
          <input type="text" id="modalInput" v-model.trim="props.isUpdateChecklistItem!.data" required>
          <button class="button-1 pl-1 pr-1">Update</button>
        </div>
      </form>
      <form 
        v-else-if="isDeleteChecklistItem"
        @submit.prevent="emits('deleteChecklistItem', modelInput)">
        <div class="d-flex space-between align-items-center mb-1">
          <p>Delete Checklist Item?<br><span class="deletesub">(Sub-tasks included)</span></p>
          <button 
            type="button" 
            class="button-2 close-btn"
            @click="emits('closeModal')"
          >
            <img class="close-img" src="@/assets/close.svg" width=30 height=30 alt="close modal">
          </button>
        </div>
        <p>{{props.descriptionRef}}</p>
        <div class="center">
          <button class="button-1">Delete</button>
        </div>
      </form>
      <form 
        v-else-if="isAddChecklistMember"
        @submit.prevent="emits('addChecklistMember', modelInput)">
        <div class="d-flex space-between align-items-center mb-1">
          <p>Add member</p>
          <button 
            type="button" 
            class="button-2 close-btn"
            @click="emits('closeModal')"
          >
            <img class="close-img" src="@/assets/close.svg" width=30 height=30 alt="close modal">
          </button>
        </div>
        <div class="d-flex gap-05 align-items-center">
          <label for="memberList">Email:</label>
          <input 
            id="memberList"
            type="text" 
            v-model.trim="modelInput"
            required 
          >
          <button class="button-1 pl-1 pr-1">Add</button>
        </div>
        <p v-if="props.userAddResponse!.length" class="text-red">{{props.userAddResponse}}</p>
        <!-- <p>Add member</p>
          <label>user: </label>
          <select v-model="modelInput">
            <option default disabled>Select user</option>
            <option v-for="member in membersStore.membersRefExceptOne">{{member.emailUser}}</option>
          </select>
        <div class="center mt-1 gap-05">
          <button>Add</button>
          <button type="button" @click="emits('closeModal')">Close</button>
        </div> -->
      </form>
    </div>
  </div>
</template>
<style scoped>
  input[type="text"]{
    padding: 0.5em;
    width: 100%;
    min-width: 5px;
  }
  .modal-back-drop{
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: rgb(0, 0, 0, 0.3);
    z-index: 3;
  }
  .modal{
    background-color: white;
    padding: 1em;
    max-width: 50vw;
  }
  @media (max-width: 500px){
    .modal{
      max-width: 80vw;
    }
  }
  .deletesub{
    color: red;
  }
</style>
------------------------
