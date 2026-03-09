import React from 'react'

const Home = () => {

    const handleGetStarted = ()=> {
        window.location.href = '/signup'
    }

    const handleLearnMore = ()=> {
        document.getElementById('features').scrollIntoView({ behavior: 'smooth' })
    }

  return (
    <div>
      <h1>Home</h1>
    </div>
  )
}

export default Home
